package ru.sberbank.jd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * The type Rest config.
 */
@Configuration
@ComponentScan("ru.sberbank.jd")
@PropertySource("classpath:application.properties")
public class RestConfig {
    /**
     * Gets rest template.
     *
     * @return the rest template
     */
    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
