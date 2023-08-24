package org.example.dao.repositories;

import org.example.dao.entities.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacationRepository extends JpaRepository<Vacation, Long> {
    List<Vacation> findByEmployeeIdAndStartDateAfter(Long employeeId, LocalDate startDate);
}