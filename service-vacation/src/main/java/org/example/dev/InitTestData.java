package org.example.dev;

import jakarta.annotation.PostConstruct;
import org.example.dao.entities.Vacation;
import org.example.dao.repositories.VacationRepository;
import org.example.services.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Profile("dev")
@Component
public class InitTestData {

    @Autowired
    private VacationService vacationService;

    @Autowired
    private VacationRepository vacationRepository;

    @PostConstruct
    public void init() {

        if (!vacationRepository.findAll().isEmpty())
            return;

        // Добавление отпуска
        Vacation vacation = new Vacation();
        vacation.setEmployeeId(1L);
        vacation.setStartDate(LocalDate.of(2023, 9, 25));
        vacation.setEndDate(LocalDate.of(2023, 9, 30));
        vacationService.addVacation(vacation);
    }
}
