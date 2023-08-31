package ru.sberbank.jd.dev;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;
import org.springframework.stereotype.Component;

@Profile("dev")
@AllArgsConstructor
@Slf4j
@Component
public class InitTestData {

    private final UserRepository userRepository;

    @PostConstruct
    public void handleStartedEvent() {
        //userRepository.deleteAll();
        if (!userRepository.findAll().isEmpty())
            return;

        log.info("Initiating test data ...");

        var user = new User("Oduvan", null,61241281L, 1L);
        userRepository.save(user);
        log.info("user added '{}'", user);
    }
}
