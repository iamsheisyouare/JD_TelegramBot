package org.example.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {

    private final String type = "Bearer";
    private String accessToken;
    //2508 закомментировал
    //private String refreshToken;
    private String telegramName;
}
