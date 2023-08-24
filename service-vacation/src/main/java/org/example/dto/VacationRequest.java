package org.example.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VacationRequest {

    private String telegramUsername;
    private LocalDate startDate;
    private LocalDate endDate;

}

