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
import ru.sberbank.jd.model.User;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Класс периодически запускается по расписанию и обновляет статусы пользователей в чате на основе данных из внешнего источника - списка сотрудников.
 */
@Slf4j
@Service
public class ChatUserJobNew {

    final IntegrationConfig integrationConfig;

    @Autowired
    UserService userService;

    @Autowired
    TelegramBot telegramBot;

    @Autowired
    EmployeeApiHandler employeeApiHandler;

    /**
     * Создает экземпляр класса ChatUserJobNew с заданной конфигурацией интеграции.
     *
     * @param integrationConfig конфигурация интеграции
     */
    public ChatUserJobNew(IntegrationConfig integrationConfig) {
        this.integrationConfig = integrationConfig;
    }

    /**
     * Метод actualizeChatUsers является запланированной задачей, которая выполняется с фиксированной задержкой, указанной в свойстве "job.scheduler.interval".
     * Он обновляет статусы пользователей чата на основе данных из микросервиса Сотрудники.
     */
    @Scheduled(fixedDelayString = "${job.scheduler.interval}")
    public void actualizeChatUsers() {
        log.info("Старт проверки актуальности пользователей чата сотрудников");

        Map<Long, EmployeeStatus> employeeList = employeeApiHandler.getListEmployeeAndStatus(integrationConfig.getAdminLogin());
        if (employeeList != null) {
            for (Long employeeId : employeeList.keySet()) {
                EmployeeStatus status = employeeList.get(employeeId);

                if (employeeId != null && telegramBot.getChatIdSet() != null) {
                    processChatUsers(employeeId, status);
                }
            }
        } else {
            log.warn("Список сотрудников = null");
        }
    }

    /**
     * Метод processChatUsers обрабатывает пользователей чата для указанного сотрудника и его статуса.
     *
     * @param employeeId ID сотрудника
     * @param status     статус сотрудника
     */
    private void processChatUsers(Long employeeId, EmployeeStatus status) {
        telegramBot.getChatIdSet().forEach(chatId -> {
            if (Long.parseLong(chatId) < 0) {
                handleChatUser(employeeId, status, chatId);
            }
        });
    }

    /**
     * Метод handleChatUser обрабатывает пользователя чата для указанного сотрудника, его статуса и ID чата.
     *
     * @param employeeId ID сотрудника
     * @param status     статус сотрудника
     * @param chatId     ID чата
     */
    private void handleChatUser(Long employeeId, EmployeeStatus status, String chatId) {
        Long userId = userService.getByEmployeeId(employeeId)
                .map(User::getTelegramUserId)
                .orElse(null);

        if (userId != null) {
            try {
                ChatMember chatMember = getChatMember(chatId, userId);
                if (chatMember != null) {
                    processChatMember(status, chatId, userId, chatMember.getStatus(), employeeId);
                }
            } catch (TelegramApiException e) {
                log.error("Не получилось определить пользователя чата:" + e);
            }
        } else {
            log.error("Не удалось определить userID по employeeId = " + employeeId.toString());
        }
    }

    /**
     * Возвращает информацию о пользователе чата для указанного ID чата и пользователя.
     *
     * @param chatId ID чата
     * @param userId ID пользователя
     * @return информация о пользователе чата
     * @throws TelegramApiException если возникла ошибка при запросе информации о пользователе чата
     */
    private ChatMember getChatMember(String chatId, Long userId) throws TelegramApiException {
        GetChatMember getChatMember = new GetChatMember(chatId, userId);
        return telegramBot.execute(getChatMember);
    }

    /**
     * Обрабатывает пользователя чата для указанного статуса сотрудника, ID чата, пользователя чата, статуса пользователя чата и ID сотрудника.
     *
     * @param status           статус сотрудника
     * @param chatId           ID чата
     * @param userId           ID пользователя чата
     * @param chatMemberStatus статус пользователя чата
     * @param employeeId       ID сотрудника
     * @throws TelegramApiException если возникла ошибка при выполнении операций с пользователем чата
     */
    private void processChatMember(EmployeeStatus status, String chatId, Long userId, String chatMemberStatus, Long employeeId) throws TelegramApiException {
        BotApiMethodBoolean chatMemberChangeStatus = null;
        String newStatus = "";

        if (!"kicked".equals(chatMemberStatus) && status.equals(EmployeeStatus.FIRED)) {
            chatMemberChangeStatus = new BanChatMember(chatId, userId);
            newStatus = "'Забанен'";
        } else if ("kicked".equals(chatMemberStatus) && status.equals(EmployeeStatus.WORK)) {
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
}
