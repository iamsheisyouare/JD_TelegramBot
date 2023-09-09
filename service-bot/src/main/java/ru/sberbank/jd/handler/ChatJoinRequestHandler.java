package ru.sberbank.jd.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeclineChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.service.TelegramBot;

/**
 * Обработчик запросов на присоединение к чату.
 */
@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ChatJoinRequestHandler {

    /**
     * Метод обработки запроса на присоединение к чату.
     *
     * @param telegramBot     Telegram бот
     * @param chatJoinRequest запрос на присоединение к чату
     * @param userFound       флаг указывающий на наличие пользователя
     */
    public void processChatJoinRequest(TelegramBot telegramBot, ChatJoinRequest chatJoinRequest, Boolean userFound) {
        String userName = chatJoinRequest.getUser().getUserName();

        log.info("Выполняется проверка ссылки приглашения");
        Long chatId = chatJoinRequest.getChat().getId();
        Long userId = chatJoinRequest.getUser().getId();
        log.info("userId = " + userId.toString() + ", chatJoinId = " + chatId);

        BotApiMethodBoolean handleChatJoinRequest;

        if (userName != null && userFound) {
            log.info("Требуется одобрение");
            handleChatJoinRequest = new ApproveChatJoinRequest(chatId.toString(), userId);
        } else {
            log.info("Требуется отклонение");
            handleChatJoinRequest = new DeclineChatJoinRequest(chatId.toString(), userId);
        }
        try {
            Boolean result = telegramBot.execute(handleChatJoinRequest);
            log.info("result = " + result.toString());
        } catch (TelegramApiException e) {
            log.error("Ошибка при одобрении/отклонении: " + e.getMessage());
        }
        log.info("Завершение одобрения/отклонения");
        responseToUser(telegramBot, userId, handleChatJoinRequest);
    }

    /**
     * Отправляет ответ пользователю о результатах запроса на присоединение к чату.
     *
     * @param telegramBot           Telegram бот
     * @param userId                идентификатор пользователя
     * @param handleChatJoinRequest объект для обработки запроса на присоединение к чату
     */
    private void responseToUser(TelegramBot telegramBot, Long userId, BotApiMethodBoolean handleChatJoinRequest) {
        String msg;
        if (handleChatJoinRequest instanceof ApproveChatJoinRequest) {
            msg = "одобрена";
        } else {
            msg = "отклонена";
        }
        try {
            telegramBot.execute(new SendMessage(userId.toString(), "Ваша заявка на присоединение к чату " + msg));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}