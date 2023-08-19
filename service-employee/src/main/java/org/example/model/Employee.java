package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.example.enums.EmployeeStatus;

@Entity
@Getter
@Setter
public class Employee {

    public Employee(){}

    public Employee(String fio, String telegramName) {
        this.fio = fio;
        this.telegramName = telegramName;
        this.status = EmployeeStatus.WORK;
    }
    public Employee(String fio, String telegramName,String password) {
        this(fio,telegramName);
        this.password = password;
    }
    @Id
    @GeneratedValue
    private Long id;
    private String fio;
    private EmployeeStatus status;
    private String telegramName;
    private String password;
    private String token;


    @ManyToMany
    private List<Role> roles;


}
