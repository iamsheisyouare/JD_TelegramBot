package org.example.services;

import org.example.dao.entities.Vacation;
import org.example.dao.repositories.VacationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class VacationService {

    @Autowired
    private VacationRepository vacationRepository;

    public void addVacation(Vacation vacation) {
        vacationRepository.save(vacation);
    }
    public void cancelVacation(Long id) {
        vacationRepository.deleteById(id);
    }
    public Vacation getVacationById(Long id) {
        return vacationRepository.findById(id).orElse(null);
    }
    public List<Vacation> getUpcomingVacationsByEmployeeId(Long employeeId) {
        return vacationRepository.findByEmployeeIdAndStartDateAfter(employeeId, LocalDate.now());
    }

}
