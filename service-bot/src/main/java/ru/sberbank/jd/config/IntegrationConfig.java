package ru.sberbank.jd.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Конфигурационный класс для интеграции.
 */
@Configuration
@Data
@PropertySource("application.properties")
public class IntegrationConfig {

    /**
     * URL для получения информации о сотрудниках.
     */
    @Value("${integration.employee.URL}")
    String employeeUrl;

    /**
     * URL для получения информации об отпусках.
     */
    @Value("${integration.vacation.URL}")
    String vacationUrl;

    /**
     * Суффикс для получения информации о сотрудниках.
     */
    @Value("${integration.employee.getSuffix}")
    String getSuffixEmployee;

    /**
     * Суффикс для получения информации об отпусках.
     */
    @Value("${integration.vacation.getSuffix}")
    String getSuffixVacation;

    /**
     * Логин администратора системы.
     */
    @Value("${integration.adminUser.login}")
    String adminLogin;

    /**
     * Пароль администратора системы.
     */
    @Value("${integration.adminUser.password}")
    String adminPassword;
}
