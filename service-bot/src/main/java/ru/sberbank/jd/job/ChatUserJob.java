package ru.sberbank.jd.job;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.enums.EmployeeStatus;
import ru.sberbank.jd.enums.UserStatus;
import ru.sberbank.jd.handler.EmployeeApiHandler;
import ru.sberbank.jd.service.TelegramBot;
import ru.sberbank.jd.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChatUserJob {

    @Autowired
    UserService userService;

    @Autowired
    TelegramBot telegramBot;

    @Autowired
    EmployeeApiHandler employeeApiHandler;

    @Scheduled(fixedDelayString = "${job.scheduler.interval}")
    public void actualizeChatUsers() {
        log.info("Старт проверки актуальности пользователей чата сотрудников");
        List<Long> employeeIdList = employeeApiHandler.getEmployeeListWithStatus(EmployeeStatus.FIRED);     // TODO список со статусами
        if (employeeIdList != null) {
            employeeIdList.forEach(employeeId -> {
                if (employeeId != null && telegramBot.getChatIdSet() != null) {
                    telegramBot.getChatIdSet().forEach(chatId -> {
                        if (Long.parseLong(chatId) < 0) {
                            UserStatus userStatus = null;
                            Long userId = null;
                            try {
                                userId = userService.getByEmployeeId(employeeId).get().getTelegramUserId();
                            }
                            catch (Exception e) {
                                log.error("Не получилось определить userID по employeeId: " + e);
                                return;
                            }
                            GetChatMember getChatMember = new GetChatMember(chatId, userId);
                            try {
                                ChatMember chatMember = telegramBot.execute(getChatMember);
                                if (chatMember != null) {
                                    BotApiMethodBoolean chatMemberChangeStatus = null;
                                    if (!"kicked".equals(chatMember.getStatus())) {
                                        userStatus = UserStatus.DELETED;
                                        userService.setStatus(employeeId, userStatus);
                                        chatMemberChangeStatus = new BanChatMember(chatId, userId);
                                    }
//                                    else if ("kicked".equals(chatMember.getStatus())) {
//                                        userStatus = UserStatus.ACTIVE;
//                                        chatMemberChangeStatus = new UnbanChatMember(chatId, userId);       // TODO здесь надо передавать не наши внутренние ид сотрудников а ид юзера телеги
//                                    }
                                    if (chatMemberChangeStatus != null) {
                                        try {
                                            telegramBot.execute(chatMemberChangeStatus);
                                            if (chatMemberChangeStatus instanceof BanChatMember) {
                                                telegramBot.prepareAndSendMessage(Long.parseLong(chatId), "Вы удалены из чата сотрудников.");   // TODO переделать на отправку в чат сотрудника, а не общий
                                            }
                                            log.info(LocalDateTime.now() + ": Изменение статуса сотрудника на " + userStatus.toString());
                                        } catch (TelegramApiException e) {
                                            log.error("Не удалось изменить статус сотрудника: " + e);
                                        }
                                    }
                                }
                            } catch (TelegramApiException e) {
                                log.error("Не получилось определить пользователя чата:" + e);
                            }
                        }
                    });
                }
            });
        }
        else {
            log.warn("Список работающих сотрудников = null");
        }
    }
}
