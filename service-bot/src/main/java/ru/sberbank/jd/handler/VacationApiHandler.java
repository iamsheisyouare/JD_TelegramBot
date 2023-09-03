package ru.sberbank.jd.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VacationApiHandler {

    private final IntegrationConfig integrationConfig;
    private final RestTemplate restTemplate;

    @Autowired
    UserService userService;

    @Autowired
    public VacationApiHandler(IntegrationConfig integrationConfig, RestTemplate restTemplate) {
        this.integrationConfig = integrationConfig;
        this.restTemplate = restTemplate;
    }

    public String handleVacationsCommand(Long employeeId) {

        ResponseEntity<Map<Long, String>> response = getUpcomingVacations(employeeId);
        //ResponseEntity<Map<Long, String>> response = getUpcomingVacations("1");
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<Long, String> vacationMap = response.getBody();
            String resultStr = "Ваши отпуска:\n";
            for (Map.Entry<Long, String> entry : vacationMap.entrySet()) {
                String value = entry.getValue();
                resultStr += value + "\n";
            }

            return resultStr;//messageBuilder.toString();
        } else {
            return "Ошибка при запросе списка отпусков.";
        }
    }
    public String handleDeleteVacationCommand(long vacationId) {
        String deleteResponse = String.valueOf(deleteVacation(vacationId));
        return deleteResponse;
    }
    public ResponseEntity<String> deleteVacation(long vacationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation/" + "/%d", vacationId),
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Отпуск успешно удален.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Отпуск не удален. Повторите запрос");
            }
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении отпуска.");
        }
    }

    public String addVacation(Long employeeId, LocalDate startDate, LocalDate endDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Форматирование дат в нужный формат YYYY-MM-dd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("employeeId", employeeId);
        requestMap.put("startDate", formattedStartDate);
        requestMap.put("endDate", formattedEndDate);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation"),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return "Отпуск успешно добавлен.";
            } else {
                return "Ошибка при добавлении отпуска: " + response.getBody();
            }
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            return "Ошибка при добавлении отпуска: " + e.getMessage();
        }
    }

    public ReplyKeyboardMarkup getVacationButtons(Long employeeId) {
        ResponseEntity<Map<Long, String>> response = getUpcomingVacations(employeeId);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<Long, String> vacationMap = response.getBody();
            if (vacationMap.isEmpty()) {
                return null; // Если нет доступных отпусков, вернем null
            }

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);
            List<KeyboardRow> keyboardRows = new ArrayList<>();

            for (Map.Entry<Long, String> entry : vacationMap.entrySet()) {
                KeyboardRow keyboardRow = new KeyboardRow();
                KeyboardButton button = new KeyboardButton(entry.getValue());
                keyboardRow.add(button);
                keyboardRows.add(keyboardRow);
            }

            keyboardMarkup.setKeyboard(keyboardRows);
            return keyboardMarkup;
        } else {
            return null;
        }
    }
    public Long getVacationIdByText(String buttonText, Long employeeId) {
        ResponseEntity<Map<Long, String>> response = getUpcomingVacations(employeeId);
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<Long, String> vacationMap = response.getBody();
            for (Map.Entry<Long, String> entry : vacationMap.entrySet()) {
                if (entry.getValue().equals(buttonText)) {
                    return entry.getKey(); // Возвращаем ID отпуска по тексту кнопки
                }
            }
        }
        return null; // Если не удалось найти соответствующий отпуск
    }
    public ResponseEntity<Map<Long, String>> getUpcomingVacations(Long employeeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation?employeeId=" + employeeId),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            /*
            //TODO переписать определение response
            ResponseEntity<Map<Long,String>> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation?telegramUsername=" + telegramUsername),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<Long, String>>() {}
            );
            //TODO переписать определение response
             */
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}