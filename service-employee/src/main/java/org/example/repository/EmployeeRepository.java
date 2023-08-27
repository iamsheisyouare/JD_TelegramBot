package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.enums.EmployeeStatus;
import org.example.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByTelegramName(String name);
    List<Employee> findByStatus(EmployeeStatus status);
}
