package ru.sberbank.jd.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Конфигурационный класс для настроек Telegram бота.
 */
@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    /**
     * Имя Telegram бота.
     */
    @Value("${bot.name}")
    String botName;

    /**
     * Токен Telegram бота.
     */
    @Value("${bot.token}")
    String token;

    /**
     * Пригласительная ссылка на Telegram бота.
     */
    @Value("${bot.inviteLink}")
    String inviteLink;

    /**
     * Интервал выполнения задачи.
     */
    @Value("${job.scheduler.interval}")
    String interval;

    /**
     * Идентификатор чата с сотрудниками.
     */
    @Value("${bot.chatId}")
    String employeeChatId;
}