package org.example.controllers;

import org.example.dao.entities.Employee;
import org.example.dao.entities.Vacation;
import org.example.dto.VacationRequest;
import org.example.services.EmployeeService;
import org.example.services.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/telegram")
public class VacationController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private VacationService vacationService;

    @PostMapping("/vacation")
    public ResponseEntity<String> addVacation(@RequestBody VacationRequest vacationRequest) {
        try {
            Employee employee = employeeService.getEmployeeByTelegramUsername(vacationRequest.getTelegramUsername());
            if (employee == null) {
                return ResponseEntity.badRequest().body("Сотрудник не найден");
            }

            Vacation vacation = new Vacation();
            vacation.setEmployee(employee);
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

    @GetMapping("/vacation")
    public ResponseEntity<Map<Long, String>> getUpcomingVacations(@RequestParam String telegramUsername) {
        try {
            Employee employee = employeeService.getEmployeeByTelegramUsername(telegramUsername);
            List<Vacation> upcomingVacations = vacationService.getUpcomingVacationsByEmployeeId(employee.getId());

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
}