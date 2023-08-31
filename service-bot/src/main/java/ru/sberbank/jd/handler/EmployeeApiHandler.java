package ru.sberbank.jd.handler;


import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.dto.UserRequest;
import ru.sberbank.jd.enums.EmployeeStatus;

import java.util.List;

@Slf4j
@Component
public class EmployeeApiHandler {

    // for test
    final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJvZHV2YW4iLCJleHAiOjE2OTM0MjQ2NTYsInJvbGVzIjpbIlJPTEVfQURNSU4iLCJST0xFX1VTRVIiXSwiZnVsbE5hbWUiOiJWYXNheSBQdXBraW4ifQ.XxuI2rEcz6tVxqf7pQRmUz6_ezCUDJaeF6wv6MapRNxRD0ewOuvat7EZwHeo5cYHiMBHoFzbhKIqZz9JEVoNTQ";
    final IntegrationConfig integrationConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public EmployeeApiHandler(RestTemplate restTemplate, IntegrationConfig integrationConfig) {
        this.restTemplate = restTemplate;
        this.integrationConfig = integrationConfig;
    }

    /**
     * Получает информацию о сотруднике по его имени в Telegram.
     *
     * @param telegramName имя в Telegram
     * @return объект EmployeeResponse с информацией о сотруднике или null, если запрашиваемый сотрудник не найден
     * @throws RuntimeException при ошибке выполнения запроса
     */
    public EmployeeResponse getEmployeeByTelegramName(String telegramName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/name/" + telegramName),
                    //"http://localhost:8001/employee/name/oduvan",
                    HttpMethod.GET,
                    entity,
                    EmployeeResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("" + e);
            return null;
        }
    }

    /**
     * Получает информацию о сотруднике по его идентификатору.
     *
     * @param employeeId идентификатор сотрудника
     * @return информация о сотруднике или null, если произошла ошибка
     */
    public EmployeeResponse getEmployeeById(Long employeeId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/" + employeeId),
                    //"http://localhost:8001/employee/name/oduvan",
                    HttpMethod.GET,
                    entity,
                    EmployeeResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("" + e);
            return null;
        }
    }

    public List<Long> getEmployeeListWithStatus(EmployeeStatus status/*, String adminToken*/) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Long[]> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/status/" + status),
                    HttpMethod.GET,
                    entity,
                    Long[].class
            );
            List<Long> employeeIdList = null;
            if (response.getBody() != null) {
                employeeIdList = List.of(response.getBody());
            }
            return employeeIdList;
        } catch (Exception e) {
            log.error("" + e);
            return null;
        }
    }

    public EmployeeResponse getAdminInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject authJsonObject = new JSONObject();
        authJsonObject.put("username", integrationConfig.getAdminLogin());
        authJsonObject.put("password", integrationConfig.getAdminPassword());

        String url = String.format(integrationConfig.getEmployeeUrl(), "login");

        HttpEntity<String> req = new HttpEntity<>(authJsonObject.toString(), headers);
        EmployeeResponse employeeResponse = restTemplate.postForObject(url, req, EmployeeResponse.class);
        return employeeResponse;
    }

    /**
     * Создает нового сотрудника с указанными данными.
     *
     * @param telegramName имя в Telegram
     * @param userFIO      ФИО пользователя
     */
    public EmployeeResponse createEmployee(String telegramName, String userFIO/*, String adminToken*/) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(telegramName);
        userRequest.setFio(userFIO);

        headers.add("Authorization", "Bearer " + token /*adminToken*/);
        HttpEntity<UserRequest> entity = new HttpEntity<>(userRequest, headers);
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee()),
                    HttpMethod.POST,
                    entity,
                    EmployeeResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("" + e);
            return null;
        }
    }

}
