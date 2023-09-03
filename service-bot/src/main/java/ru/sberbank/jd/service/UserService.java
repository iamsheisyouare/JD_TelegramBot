package ru.sberbank.jd.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sberbank.jd.dto.UserRequest;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> getByTelegramName(@NonNull String telegramName) {
        return userRepository.findByTelegramName(telegramName);
    }

    public Optional<User> getByEmployeeId(@NonNull Long getByEmployeeId) {
        return userRepository.findByEmployeeId(getByEmployeeId);
    }

    public ResponseEntity<User> createUser(UserRequest userRequest) {
        User user = new User(userRequest.getUsername());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    public User createNewUser(String telegramName, String token, Long telegramUserId, Long employeeId) {
        User user = new User(telegramName, token, telegramUserId, employeeId);
        User saved = userRepository.save(user);
        return saved;
    }

    public User setEmployeeInfo(String telegramName, String token, Long telegramUserId, Long employeeId) {

        if (getByTelegramName(telegramName).isPresent()){
            User user = getByTelegramName(telegramName).get();
            user.setToken(token);
            user.setEmployeeId(employeeId);
            User saved = userRepository.save(user);
            return saved;
        }
        else{
            User user = createNewUser(telegramName, token, telegramUserId, employeeId);
            User saved = userRepository.save(user);
            return saved;
        }
    }

    public ResponseEntity<User> findById(Long id)
    {
        return ResponseEntity.ok(userRepository.findById(id).orElse(null));
    }

    public String getUserToken(String telegramName)
    {
        if (getByTelegramName(telegramName).isPresent()){
            User user = getByTelegramName(telegramName).get();
            return user.getToken();
        }
        else{
            return "";      // TODO возвращаем пустую строку
        }
    }

}
