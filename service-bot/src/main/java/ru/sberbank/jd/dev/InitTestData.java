package ru.sberbank.jd.dev;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class InitTestData {

    private final UserRepository userRepository;

    @PostConstruct
    public void handleStartedEvent() {
        userRepository.deleteAll();

        log.info("Initiating test data ...");

        var user = new User("oduvan");
        userRepository.save(user);
        log.info("user added '{}'", user);
    }
}
