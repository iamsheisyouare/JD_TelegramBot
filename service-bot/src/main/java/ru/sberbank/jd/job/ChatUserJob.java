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
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.enums.EmployeeStatus;
import ru.sberbank.jd.handler.EmployeeApiHandler;
import ru.sberbank.jd.service.TelegramBot;
import ru.sberbank.jd.service.UserService;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class ChatUserJob {

    final IntegrationConfig integrationConfig;

    @Autowired
    UserService userService;

    @Autowired
    TelegramBot telegramBot;

    @Autowired
    EmployeeApiHandler employeeApiHandler;

    public ChatUserJob(IntegrationConfig integrationConfig) {
        this.integrationConfig = integrationConfig;
    }

//    @Scheduled(fixedDelayString = "${job.scheduler.interval}")
    public void actualizeChatUsers() {
        log.info("Старт проверки актуальности пользователей чата сотрудников");
        Map<Long, EmployeeStatus> employeeList = employeeApiHandler.getListEmployeeAndStatus(integrationConfig.getAdminLogin());
        if (employeeList != null) {
            employeeList.forEach((employeeId, status) -> {
                if (employeeId != null && telegramBot.getChatIdSet() != null) {
                    telegramBot.getChatIdSet().forEach(chatId -> {          // TODO убрать перебор чатов - ходим по одному
                        if (Long.parseLong(chatId) < 0) {
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
                                    String newStatus = "";
                                    if (!"kicked".equals(chatMember.getStatus()) && status.equals(EmployeeStatus.FIRED)) {
                                        chatMemberChangeStatus = new BanChatMember(chatId, userId);
                                        newStatus = "'Забанен'";
                                    }
                                    else if ("kicked".equals(chatMember.getStatus()) && status.equals(EmployeeStatus.WORK)) {   // TODO Какой все таки статус? И если left сделать обработку тоже
                                        chatMemberChangeStatus = new UnbanChatMember(chatId, userId);
                                        newStatus = "'Разбанен'";
                                    }
                                    if (chatMemberChangeStatus != null) {
                                        try {
                                            telegramBot.execute(chatMemberChangeStatus);
                                            if (chatMemberChangeStatus instanceof BanChatMember) {
                                                telegramBot.prepareAndSendMessage(userId, "Вы удалены из чата сотрудников.");
                                            }
                                            log.info(LocalDateTime.now() + ": Изменение статуса сотрудника с ID = " + employeeId + " на " + newStatus);
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
            log.warn("Список сотрудников = null");
        }
    }
}
