package org.example.controllers;

import org.example.dao.entities.Vacation;
import org.example.dto.VacationRequest;
import org.example.services.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/telegram")
public class VacationController {

    @Autowired
    private VacationService vacationService;
    @GetMapping("/vacation")
    public ResponseEntity<Map<Long, String>> getUpcomingVacations(@RequestParam Long employeeId) {
        try {
            List<Vacation> upcomingVacations = vacationService.getUpcomingVacationsByEmployeeId(employeeId);

            // проверка если нет отпусков в БД
            if (upcomingVacations.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyMap());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            Map<Long, String> vacationMap = upcomingVacations.stream()
                    .sorted(Comparator.comparing(Vacation::getStartDate))
                    .collect(Collectors.toMap(
                            Vacation::getId,
                            vacation -> vacation.getStartDate().format(formatter) + " - " + vacation.getEndDate().format(formatter),
                            (existing, replacement) -> existing, // для обработки дубликатов ключей
                            LinkedHashMap::new // Для поддержания порядка вставки
                    ));

            return ResponseEntity.ok(vacationMap);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/vacation")
    public ResponseEntity<String> addVacation(@RequestBody VacationRequest vacationRequest) {
        try {

            List<Vacation> existingVacations = vacationService.getUpcomingVacationsByEmployeeId(vacationRequest.getEmployeeId());
            int totalVacationDays = existingVacations.stream()
                    .mapToInt(vacation -> vacation.getEndDate().getDayOfYear() - vacation.getStartDate().getDayOfYear() + 1)
                    .sum();

            int newVacationDays = vacationRequest.getEndDate().getDayOfYear() - vacationRequest.getStartDate().getDayOfYear() + 1;
            if (totalVacationDays + newVacationDays > 28) {
                return ResponseEntity.badRequest().body("Общая продолжительность отпусков не должна превышать 28 дней");
            }

            // Форматирование дат в нужный формат YYYY-MM-dd
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            String formattedStartDate = vacationRequest.getStartDate().format(formatter);
//            String formattedEndDate = vacationRequest.getEndDate().format(formatter);
//            vacation.setStartDate(LocalDate.parse(formattedStartDate));
//            vacation.setEndDate(LocalDate.parse(formattedEndDate));

            Vacation vacation = new Vacation();
            vacation.setEmployeeId(vacationRequest.getEmployeeId());
            vacation.setStartDate(vacationRequest.getStartDate());
            vacation.setEndDate(vacationRequest.getEndDate());

            vacationService.addVacation(vacation);

            return ResponseEntity.ok("Отпуск добавлен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }

    @DeleteMapping("/vacation/{id}")
    public ResponseEntity<String> cancelVacation(@PathVariable Long id) {
        try {
            Vacation vacation = vacationService.getVacationById(id);
            if (vacation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Отпуск не найден");
            }

            vacationService.cancelVacation(id);
            return ResponseEntity.ok("Отпуск отменен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка");
        }
    }
}