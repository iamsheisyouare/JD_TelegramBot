package ru.sberbank.jd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.sberbank.jd.service.TelegramBot;

/**
 * Класс для инициализации Telegram бота.
 */
@Slf4j
@Component
public class BotInitializer {

    @Autowired
    TelegramBot bot;

    /**
     * Метод инициализации Telegram бота при старте приложения.
     *
     * @throws TelegramApiException возникает, если произошла ошибка при регистрации бота.
     */
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Произошла ошибка: " + e.getMessage());
        }
    }
}
