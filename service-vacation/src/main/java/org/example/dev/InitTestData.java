package org.example.dev;

import jakarta.annotation.PostConstruct;
import org.example.dao.entities.Employee;
import org.example.dao.entities.Vacation;
import org.example.services.EmployeeService;
import org.example.services.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Profile("dev")
@Component
public class InitTestData {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private VacationService vacationService;

    @PostConstruct
    public void init() {

        // Добавление сотрудника
        Employee employee = new Employee();
        employee.setName("Pupa Zotov");
        employee.setTelegramUsername("pupazotov");
        employeeService.addEmployee(employee);

        // Добавление отпуска
        Vacation vacation = new Vacation();
        vacation.setEmployee(employee);
        vacation.setStartDate(LocalDate.of(2024, 9, 15));
        vacation.setEndDate(LocalDate.of(2024, 9, 30));
        vacationService.addVacation(vacation);
    }
}
