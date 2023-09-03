package org.example.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VacationRequest {

    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;

}

