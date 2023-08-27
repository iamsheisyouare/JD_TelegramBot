package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.enums.EmployeeStatus;

@Getter
@Setter
public class EmployeeResponse {

    public EmployeeResponse(String name, Long id, String token,EmployeeStatus status,String fio) {
        this.name = name;
        this.id = id;
        this.token = token;
        this.status = status;
        this.fio = fio;
    }

    private String name;
    private String fio;
    private Long id;
    private String token;
    private EmployeeStatus status;

}
