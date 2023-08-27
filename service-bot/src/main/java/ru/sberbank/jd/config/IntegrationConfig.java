package ru.sberbank.jd.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class IntegrationConfig {

    @Value("${integration.employee.URL}")
    String employeeUrl;

    @Value("${integration.vacation.URL}")
    String vacationUrl;

    @Value("${integration.employee.getSuffix}")
    String getSuffixEmployee;

    @Value("${integration.vacation.getSuffix}")
    String getSuffixVacation;

    @Value("${integration.adminUser.login}")
    String adminLogin;

    @Value("${integration.adminUser.password}")
    String adminPassword;
}
