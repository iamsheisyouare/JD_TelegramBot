package ru.sberbank.jd.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;

import java.util.Map;

@Slf4j
@Component
public class VacationApiHandler {

    private final IntegrationConfig integrationConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public VacationApiHandler(IntegrationConfig integrationConfig, RestTemplate restTemplate) {
        this.integrationConfig = integrationConfig;
        this.restTemplate = restTemplate;
    }

    public String handleVacationsCommand(String telegramUsername) {
        EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig);
        EmployeeResponse employeeResponse = employeeApiHandler.getEmployeeByTelegramName(telegramUsername);
        if (employeeResponse == null) {
            return "Сотрудник не найден";
        }

        ResponseEntity<Map<Long, String>> response = getUpcomingVacations(employeeResponse.getName());
        //ResponseEntity<Map<Long, String>> response = getUpcomingVacations("Oduvan");
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

    public ResponseEntity<Map<Long, String>> getUpcomingVacations(String telegramUsername) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    String.format(integrationConfig.getVacationUrl(), integrationConfig.getGetSuffixVacation() + "/vacation?telegramUsername=" + telegramUsername),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}