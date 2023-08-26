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

    public ResponseEntity<User> createUser(UserRequest userRequest) {
        User user = new User(userRequest.getUsername());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    public ResponseEntity<User> findById(Long id)
    {
        return ResponseEntity.ok(userRepository.findById(id).orElse(null));
    }

}
