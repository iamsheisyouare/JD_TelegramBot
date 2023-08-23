package ru.sberbank.jd.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.jd.enums.UserStatus;

@Entity
@Getter
@Setter
@Table(name="TelegramUser")
public class User {

    public User() {
    }

    public User(String telegramName) {
        this.telegramName = telegramName;
        this.status = UserStatus.ACTIVE;
        this.isDeleted = false;
    }

    public User(String telegramName, String token) {
        this.telegramName = telegramName;
        this.status = UserStatus.ACTIVE;
        this.isDeleted = false;
        this.token = token;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String telegramName;
    private UserStatus status;
    private String token;
    private Boolean isDeleted;

//    ID	int	табельный номер - ID из схемы Сотрудников из таблицы Employee
//    telegram_name	varchar	Имя пользователя в телеграм
//    link_send	boolean	была ли отправлена ссылка на вступление в чат
//    token	varchar	токен полуенный от сервиса аутентификации
//    isDeleted	boolean	флаг удаления из чата

}
