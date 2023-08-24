package org.example.dao.repositories;

import org.example.dao.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByTelegramUsername(String telegramUsername);
}
