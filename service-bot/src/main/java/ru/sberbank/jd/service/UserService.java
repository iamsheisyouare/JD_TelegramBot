package ru.sberbank.jd.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sberbank.jd.dto.UserRequest;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;

import java.util.Optional;

/**
 * Класс UserService представляет сервис для работы с пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Получает пользователя по его Telegram-имени.
     *
     * @param telegramName Telegram-имя пользователя
     * @return Optional с пользователем, если найден, или пустой Optional, если не найден
     */
    public Optional<User> getByTelegramName(@NonNull String telegramName) {
        return userRepository.findByTelegramName(telegramName);
    }

    /**
     * Получает пользователя по его ID сотрудника.
     *
     * @param employeeId ID сотрудника
     * @return Optional с пользователем, если найден, или пустой Optional, если не найден
     */
    public Optional<User> getByEmployeeId(@NonNull Long employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    /**
     * Создает нового пользователя.
     *
     * @param userRequest запрос пользователя
     * @return ResponseEntity с созданным пользователем
     */
    public ResponseEntity<User> createUser(UserRequest userRequest) {
        User user = new User(userRequest.getUsername());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    /**
     * Создает нового пользователя с указанными параметрами.
     *
     * @param telegramName   Telegram-имя пользователя
     * @param token          токен пользователя
     * @param telegramUserId ID пользователя в Telegram
     * @param employeeId     ID сотрудника
     * @return созданный пользователь
     */
    public User createNewUser(String telegramName, String token, Long telegramUserId, Long employeeId) {
        User user = new User(telegramName, token, telegramUserId, employeeId);
        User saved = userRepository.save(user);
        return saved;
    }

    /**
     * Создает нового пользователя с указанными параметрами (без ID Telegram).
     *
     * @param telegramName Telegram-имя пользователя
     * @param token        токен пользователя
     * @param employeeId   ID сотрудника
     * @return созданный пользователь
     */
    public User createNewUser(String telegramName, String token, Long employeeId) {
        User user = new User(telegramName, token, employeeId);
        User saved = userRepository.save(user);
        return saved;
    }

    /**
     * Устанавливает информацию о сотруднике для пользователя с указанным Telegram-именем.
     *
     * @param telegramName Telegram-имя пользователя
     * @param token        токен пользователя
     * @param employeeId   ID сотрудника
     * @return обновленный пользователь
     */
    public User setEmployeeInfo(String telegramName, String token, Long employeeId) {

        if (getByTelegramName(telegramName).isPresent()) {
            User user = getByTelegramName(telegramName).get();
            user.setToken(token);
            user.setEmployeeId(employeeId);
            User saved = userRepository.save(user);
            return saved;
        } else {
            User user = createNewUser(telegramName, token, employeeId);
            User saved = userRepository.save(user);
            return saved;
        }
    }

    /**
     * Устанавливает ID пользователя в Telegram для пользователя с указанным Telegram-именем.
     *
     * @param telegramName   Telegram-имя пользователя
     * @param telegramUserId ID пользователя в Telegram
     * @return обновленный пользователь
     */
    public User setTelegramUserId(String telegramName, Long telegramUserId) {

        if (getByTelegramName(telegramName).isPresent()) {
            User user = getByTelegramName(telegramName).get();
            user.setTelegramUserId(telegramUserId);
            User saved = userRepository.save(user);
            return saved;
        } else {
            return null;
        }
    }

    /**
     * Находит пользователя по его ID.
     *
     * @param id ID пользователя
     * @return ResponseEntity с найденным пользователем или пустым значением, если не найден
     */
    public ResponseEntity<User> findById(Long id) {
        return ResponseEntity.ok(userRepository.findById(id).orElse(null));
    }

    /**
     * Возвращает токен пользователя по его Telegram-имени.
     *
     * @param telegramName Telegram-имя пользователя
     * @return токен пользователя
     */
    public String getUserToken(String telegramName) {
        if (getByTelegramName(telegramName).isPresent()) {
            User user = getByTelegramName(telegramName).get();
            return user.getToken();
        } else {
            return "";
        }
    }

}
