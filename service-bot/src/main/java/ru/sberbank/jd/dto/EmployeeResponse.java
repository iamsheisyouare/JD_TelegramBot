package ru.sberbank.jd.dto;

import lombok.Getter;
import lombok.Setter;
import ru.sberbank.jd.enums.EmployeeStatus;

/**
 * DTO (Data Transfer Object) для ответа о сотруднике.
 */
@Setter
@Getter
public class EmployeeResponse {

    private String name;           // Имя сотрудника
    private String fio;            // Полное имя сотрудника
    private Long id;               // Идентификатор сотрудника
    private String token;          // Токен сотрудника
    private EmployeeStatus status; // Статус сотрудника
}