package ru.sberbank.jd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурационный класс для настройки REST.
 */
@Configuration
@ComponentScan("ru.sberbank.jd")
@PropertySource("classpath:application.properties")
public class RestConfig {

    /**
     * Возвращает экземпляр RestTemplate.
     *
     * @return экземпляр RestTemplate
     */
    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
