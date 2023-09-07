package ru.sberbank.jd.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Конфигурационный класс для настроек задачи.
 */
@Configuration
@Data
@PropertySource("application.properties")
public class JobConfig {

    /**
     * Интервал выполнения задачи.
     */
    @Value("${job.scheduler.interval}")
    String interval;
}
