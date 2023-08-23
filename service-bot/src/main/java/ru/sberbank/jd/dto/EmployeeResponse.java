package ru.sberbank.jd.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmployeeResponse {
    private String name;
    private Long id;
    private String token;
}
