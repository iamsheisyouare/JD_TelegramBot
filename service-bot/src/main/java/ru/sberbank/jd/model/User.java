package ru.sberbank.jd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.jd.enums.UserStatus;

@Entity
@Getter
@Setter
@Table(name="TelegramUser")
public class User {

    public User(){}

    public User(String telegramName) {
        this.telegramName = telegramName;
        this.status = UserStatus.ACTIVE;
    }

    @Id
    @GeneratedValue
    private Long id;
    private UserStatus status;
    private String telegramName;
    private String token;

//    ID	int	табельный номер - ID из схемы Сотрудников из таблицы Employee
//    telegram_name	varchar	Имя пользователя в телеграм
//    link_send	boolean	была ли отправлена ссылка на вступление в чат
//    token	varchar	токен полуенный от сервиса аутентификации
//    isDeleted	boolean	флаг удаления из чата

}
