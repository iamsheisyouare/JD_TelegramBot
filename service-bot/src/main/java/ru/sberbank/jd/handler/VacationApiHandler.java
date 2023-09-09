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
import ru.sberbank.jd.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обработчик API для работы с отпусками.
 */
@Slf4j
@Component
public class VacationApiHandler {

    private final IntegrationConfig integrationConfig;
    private final RestTemplate restTemplate;

    @Autowired
    UserService userService;

    /**
     * Конструктор класса VacationApiHandler.
     *
     * @param integrationConfig конфигурация интеграции
     * @param restTemplate      экземпляр RestTemplate для отправки запросов
     */
    @Autowired
    public VacationApiHandler(IntegrationConfig integrationConfig, RestTemplate restTemplate) {
        this.integrationConfig = integrationConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * Обрабатывает команду на получение списка отпусков.
     *
     * @param employeeId идентификатор сотрудника
     * @return список предстоящих отпусков или сообщение об ошибке
     */
    public String handleVacationsCommand(Long employeeId) {

        ResponseEntity<Map<Long, String>> response = getUpcomingVacations(employeeId);
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<Long, String> vacationMap = response.getBody();

            if (vacationMap.isEmpty()) {
                return "У вас нет предстоящих отпусков.";
            }

            StringBuilder resultStr = new StringBuilder("Ваши отпуска:\n");
            for (Map.Entry<Long, String> entry : vacationMap.entrySet()) {
                resultStr.append(entry.getValue()).append("\n");
            }

            return resultStr.toString();
        } else {
            return "Ошибка при запросе списка отпусков.";
        }
    }

    /**
     * Удаляет отпуск по указанному идентификатору.
     *
     * @param vacationId идентификатор отпуска
     * @return сообщение об успешном удалении или ошибке
     */
    public String deleteVacation(long vacationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation/" + vacationId),
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return "Отпуск успешно удален.";
            } else {
                return "Отпуск не удален. Повторите запрос: " + response.getBody();
            }
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return "Ошибка при удалении отпуска: " + e.getMessage();
        }
    }

    /**
     * Добавляет отпуск для указанного сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @param startDate  дата начала отпуска
     * @param endDate    дата окончания отпуска
     * @return сообщение об успешном добавлении или ошибке
     */
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
            log.error("Ошибка: " + e.getMessage());
            return "Ошибка при добавлении отпуска: " + e.getMessage();
        }
    }

    /**
     * Возвращает кнопки отпусков в виде ReplyKeyboardMarkup для указанного сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @return ReplyKeyboardMarkup с кнопками отпусков или null, если отпусков нет
     */
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

    /**
     * Возвращает идентификатор отпуска на основе текста кнопки.
     *
     * @param buttonText текст кнопки
     * @param employeeId идентификатор сотрудника
     * @return идентификатор отпуска или null, если не удалось найти соответствующий отпуск
     */
    public Long getVacationIdByText(String buttonText, Long employeeId) {
        ResponseEntity<Map<Long, String>> response = getUpcomingVacations(employeeId);
        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            Map<Long, String> vacationMap = response.getBody();
            if (vacationMap != null) {
                for (Map.Entry<Long, String> entry : vacationMap.entrySet()) {
                    if (entry.getValue().equals(buttonText)) {
                        try {
                            return entry.getKey();
                        } catch (Exception e) {
                            return null;
                        }
                    }
                }
            }
        }
        return null; // Если не удалось найти соответствующий отпуск
    }

    /**
     * Отправляет HTTP-запрос для получения предстоящих отпусков сотрудника.
     *
     * @param employeeId идентификатор сотрудника
     * @return ResponseEntity с картой отпусков или ResponseEntity с ошибкой в случае неудачи
     */
    public ResponseEntity<Map<Long, String>> getUpcomingVacations(Long employeeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<Long, String>> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation?employeeId=" + employeeId),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<Long, String>>() {
                    }
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}