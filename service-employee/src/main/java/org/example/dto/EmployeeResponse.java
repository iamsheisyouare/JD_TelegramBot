package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.enums.EmployeeStatus;

@Getter
@Setter
public class EmployeeResponse {

    public EmployeeResponse(String name, Long id, String token,EmployeeStatus status) {
        this.name = name;
        this.id = id;
        this.token = token;
        this.status = status;
    }

    private String name;
    private Long id;
    private String token;
    private EmployeeStatus status;

}
