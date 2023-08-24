package org.example.services;

import org.example.dao.entities.Employee;
import org.example.dao.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee getEmployeeByTelegramUsername(String telegramUsername) {
        return employeeRepository.findByTelegramUsername(telegramUsername);
    }

}
