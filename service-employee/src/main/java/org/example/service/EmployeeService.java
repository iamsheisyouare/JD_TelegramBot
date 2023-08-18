package org.example.service;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.model.Employee;
import org.example.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class EmployeeService {

        private final EmployeeRepository employeeRepository;

        public Optional<Employee> getByName(@NonNull String username) {
            return employeeRepository.findByTelegramName(username);
        }

}
