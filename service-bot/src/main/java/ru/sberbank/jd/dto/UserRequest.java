package ru.sberbank.jd.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequest {
    private String telegramName;
    private String userFIO;
}
