package ru.sberbank.jd.dev;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.handler.EmployeeApiHandler;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;
import org.springframework.stereotype.Component;
import ru.sberbank.jd.service.UserService;

/**
 * Класс для инициализации тестовых данных в dev среде.
 */
@Profile("dev")
@AllArgsConstructor
@Slf4j
@Component
public class InitTestData {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final IntegrationConfig integrationConfig;

    /**
     * Метод, вызываемый после создания компонента.
     * Инициализирует тестовые данные, если база данных пользователей пуста.
     */
    @PostConstruct
    public void handleStartedEvent() {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        log.info("Инициализация тестовых данных ...");

        EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig, userService);

        var user = new User("Oduvan", employeeApiHandler.getAdminInfo().getToken(), 61241281L, 1L);
        userRepository.save(user);
        log.info("Добавлен пользователь '{}'", user);

        String telegramName = "iamheisyouare";
        String adminLogin = integrationConfig.getAdminLogin();
        String token = employeeApiHandler.getEmployeeByTelegramName(telegramName, adminLogin).getToken();
        user = new User(telegramName, token, 65532138L, 2L);
        userRepository.save(user);
        log.info("Добавлен пользователь '{}'", user);
    }
}
