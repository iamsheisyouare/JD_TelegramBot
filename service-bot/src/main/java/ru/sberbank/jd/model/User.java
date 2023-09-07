package ru.sberbank.jd.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Модель пользователя.
 */
@Entity
@Getter
@Setter
@Table(name = "TelegramUser")
public class User {

    public User() {
    }

    /**
     * Конструктор для создания пользователя с указанным именем в Telegram.
     *
     * @param telegramName имя пользователя в Telegram
     */
    public User(String telegramName) {
        this.telegramName = telegramName;
        this.isDeleted = false;
    }

    /**
     * Конструктор для создания пользователя с указанным именем и идентификатором в Telegram.
     *
     * @param telegramName   имя пользователя в Telegram
     * @param telegramUserId идентификатор пользователя в Telegram
     */
    public User(String telegramName, Long telegramUserId) {
        this.telegramName = telegramName;
        this.isDeleted = false;
        this.telegramUserId = telegramUserId;
    }

    /**
     * Конструктор для создания пользователя с указанными именем, токеном и идентификатором сотрудника.
     *
     * @param telegramName имя пользователя в Telegram
     * @param token        токен пользователя
     * @param employeeId   идентификатор сотрудника
     */
    public User(String telegramName, String token, Long employeeId) {
        this.telegramName = telegramName;
        this.token = token;
        this.isDeleted = false;
        this.employeeId = employeeId;
    }

    /**
     * Конструктор для создания пользователя с указанными именем, токеном, идентификатором в Telegram и идентификатором сотрудника.
     *
     * @param telegramName   имя пользователя в Telegram
     * @param token          токен пользователя
     * @param telegramUserId идентификатор пользователя в Telegram
     * @param employeeId     идентификатор сотрудника
     */
    public User(String telegramName, String token, Long telegramUserId, Long employeeId) {
        this.telegramName = telegramName;
        this.token = token;
        this.isDeleted = false;
        this.telegramUserId = telegramUserId;
        this.employeeId = employeeId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telegramName;
    private String token;
    private Boolean isDeleted;
    private Long telegramUserId;
    private Long employeeId;

}
