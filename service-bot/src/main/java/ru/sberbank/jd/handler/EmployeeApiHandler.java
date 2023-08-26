package ru.sberbank.jd.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.dto.UserRequest;

import java.util.HashMap;
import java.util.Map;

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
                    String.format(integrationConfig.getUrl(), integrationConfig.getGetSuffixEmployee() + "/name/" + telegramName),
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
                    String.format(integrationConfig.getUrl(), integrationConfig.getGetSuffixEmployee() + "/" + employeeId),
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
     * Создает нового сотрудника с указанными данными.
     *
     * @param telegramName имя в Telegram
     * @param userFIO      ФИО пользователя
     */
    public void createEmployee(String telegramName, String userFIO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + token);

        UserRequest userRequest = new UserRequest();
        userRequest.setTelegramName(telegramName);
        userRequest.setUserFIO(userFIO);

        HttpEntity<UserRequest> entity = new HttpEntity<>(userRequest, headers);
        log.info("Create User Request: " + entity.toString());
        try {
            restTemplate.exchange(
                    String.format(integrationConfig.getUrl(), integrationConfig.getGetSuffixEmployee()),
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );
        } catch (HttpClientErrorException.Forbidden ex) {
            // Обрабатываем ошибку 403 (Forbidden)
            log.error("Ошибка 403: Доступ запрещен! | " + ex);
        } catch (Exception e) {
            log.error("" + e);
        }
    }
//    public void createEmployee(String telegramName, String userFIO) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.add("Authorization", "Bearer " + token);
//        // TODO необходимо использовать UserRequest
//        Map<String, String> requestBody = new HashMap<>();
//        requestBody.put("telegramName", telegramName);
//        requestBody.put("userFIO", userFIO);
//
//        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
//        try {
//            restTemplate.exchange(
//                    String.format(integrationConfig.getUrl(), integrationConfig.getGetSuffixEmployee()),
//                    HttpMethod.PUT,
//                    entity,
//                    String.class
//            );
//        } catch (Exception e) {
//            log.error("" + e);
//        }
//    }

}
