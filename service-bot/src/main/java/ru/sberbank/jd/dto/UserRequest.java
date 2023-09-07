package ru.sberbank.jd.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) для запроса пользователя.
 */
@Setter
@Getter
public class UserRequest {
    private String fio;         // Полное имя пользователя
    private String username;    // Имя пользователя
}
