package ru.sberbank.jd.dto;

import lombok.Getter;
import lombok.Setter;
import ru.sberbank.jd.enums.EmployeeStatus;

@Setter
@Getter
public class EmployeeResponse {

    private String name;
    private String fio;
    private Long id;
    private String token;
    private EmployeeStatus status;
}