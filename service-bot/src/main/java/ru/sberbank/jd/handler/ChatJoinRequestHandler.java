package ru.sberbank.jd.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeclineChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.service.TelegramBot;

/**
 * The type Chat join request handler.
 */
@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ChatJoinRequestHandler {

    /**
     * Process chat join request bot api method.
     *

     * @param chatJoinRequest the chat join request
     * @return the bot api method
     */
    public void processChatJoinRequest(TelegramBot telegramBot, ChatJoinRequest chatJoinRequest, Boolean userFound) {
        String userName = chatJoinRequest.getUser().getUserName();

        log.info("Зашли в проверку ссылки приглашения");
        Long chatId = chatJoinRequest.getChat().getId();
        Long userId = chatJoinRequest.getUser().getId();
        log.info("string userId = " + userId.toString() + ", chatJoinId = " + chatId + ", telegramBot chatID = " );

        BotApiMethodBoolean handleChatJoinRequest;

        if (userName != null && userFound) {
            log.info("Approve need");
            handleChatJoinRequest = new ApproveChatJoinRequest(chatId.toString(), userId);
        } else {
            log.info("Decline need");
            handleChatJoinRequest = new DeclineChatJoinRequest(chatId.toString(), userId);
        }
        try{
            Boolean result = telegramBot.execute(handleChatJoinRequest);
            log.info("result = " + result.toString());
        }
        catch (TelegramApiException e){
            log.error("Approve/Decline failed: " + e.getMessage());
        }
        log.info("Approve/Decline finish");
        responseToUser(telegramBot, userId, handleChatJoinRequest);
    }

    private void responseToUser(TelegramBot telegramBot, Long userId, BotApiMethodBoolean handleChatJoinRequest) {
        String msg;
        if (handleChatJoinRequest instanceof ApproveChatJoinRequest) {
            msg = "одобрена";
        } else {
            msg = "отклонена";
        }
        try {
            telegramBot.execute(new SendMessage(userId.toString(), "Ваша заявка на вступление в группу " + msg));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
