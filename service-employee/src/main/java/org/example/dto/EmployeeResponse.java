package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponse {

    public EmployeeResponse(String name, Long id, String token) {
        this.name = name;
        this.id = id;
        this.token = token;
    }

    private String name;
    private Long id;
    private String token;

}
