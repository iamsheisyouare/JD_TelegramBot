package ru.sberbank.jd.repository;

import ru.sberbank.jd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для доступа к данным пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Найти пользователя по имени в Telegram.
     *
     * @param name имя пользователя в Telegram
     * @return Optional объект с найденным пользователем или пустой, если пользователь не найден
     */
    Optional<User> findByTelegramName(String name);

    /**
     * Найти пользователя по идентификатору сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @return Optional объект с найденным пользователем или пустой, если пользователь не найден
     */
    Optional<User> findByEmployeeId(Long employeeId);
}
