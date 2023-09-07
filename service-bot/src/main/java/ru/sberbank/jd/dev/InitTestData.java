package ru.sberbank.jd.dev;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Класс для инициализации тестовых данных в dev среде.
 */
@Profile("dev")
@AllArgsConstructor
@Slf4j
@Component
public class InitTestData {

    private final UserRepository userRepository;

    /**
     * Метод, вызываемый после создания компонента.
     * Инициализирует тестовые данные, если база данных пользователей пуста.
     */
    @PostConstruct
    public void handleStartedEvent() {
        //userRepository.deleteAll();
        if (!userRepository.findAll().isEmpty())
            return;

        log.info("Инициализация тестовых данных ...");

        var user = new User("Oduvan", null, 61241281L, 1L);
        userRepository.save(user);
        log.info("Добавлен пользователь '{}'", user);

        user = new User("iamheisyouare", null, 65532138L, 2L);
        userRepository.save(user);
        log.info("Добавлен пользователь '{}'", user);
    }
}
