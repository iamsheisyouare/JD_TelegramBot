package ru.sberbank.jd.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="TelegramUser")
public class User {

    public User() {
    }

    public User(String telegramName) {
        this.telegramName = telegramName;
        this.isDeleted = false;
    }

    public User(String telegramName, String token) {
        this.telegramName = telegramName;
        this.isDeleted = false;
        this.token = token;
    }

    public User(String telegramName, Long telegramUserId) {
        this.telegramName = telegramName;
        this.isDeleted = false;
        this.telegramUserId = telegramUserId;
    }

    public User(String telegramName, String token, Long telegramUserId, Long employeeId) {
        this.telegramName = telegramName;
        this.token = token;
        this.isDeleted = false;
        this.telegramUserId = telegramUserId;
        this.employeeId = employeeId;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String telegramName;
    private String token;
    private Boolean isDeleted;
    private Long telegramUserId;
    private Long employeeId;

//    ID	int	табельный номер - ID из схемы Сотрудников из таблицы Employee
//    telegram_name	varchar	Имя пользователя в телеграм
//    link_send	boolean	была ли отправлена ссылка на вступление в чат
//    token	varchar	токен полуенный от сервиса аутентификации
//    isDeleted	boolean	флаг удаления из чата

}
