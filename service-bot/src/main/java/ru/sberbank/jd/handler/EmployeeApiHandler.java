package ru.sberbank.jd.handler;


import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.dto.UserRequest;
import ru.sberbank.jd.enums.EmployeeStatus;
import ru.sberbank.jd.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * Класс, отвечающий за обработку API запросов, связанных со сотрудниками.
 */
@Slf4j
@Component
public class EmployeeApiHandler {

    final IntegrationConfig integrationConfig;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Autowired
    public EmployeeApiHandler(RestTemplate restTemplate, IntegrationConfig integrationConfig, UserService userService) {
        this.restTemplate = restTemplate;
        this.integrationConfig = integrationConfig;
        this.userService = userService;
    }

    /**
     * Создает объект HttpHeaders с авторизационным заголовком.
     *
     * @param userTelegramName имя пользователя в Telegram
     * @return объект HttpHeaders с установленным авторизационным заголовком
     */
    private HttpHeaders createHeaders(String userTelegramName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION_HEADER, "Bearer " + userService.getUserToken(userTelegramName));
        return headers;
    }

    /**
     * Получает информацию о сотруднике по его имени в Telegram.
     *
     * @param telegramName     имя в Telegram
     * @param userTelegramName имя пользователя в Telegram
     * @return объект EmployeeResponse с информацией о сотруднике или null, если запрашиваемый сотрудник не найден
     * @throws RuntimeException при ошибке выполнения запроса
     */
    public EmployeeResponse getEmployeeByTelegramName(String telegramName, String userTelegramName) {
        HttpHeaders headers = createHeaders(userTelegramName);
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/name/" + telegramName),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
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
     * @param employeeId       идентификатор сотрудника
     * @param userTelegramName имя пользователя в Telegram
     * @return информация о сотруднике или null, если произошла ошибка
     */
    public EmployeeResponse getEmployeeById(Long employeeId, String userTelegramName) {
        HttpHeaders headers = createHeaders(userTelegramName);
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/" + employeeId),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    EmployeeResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("" + e);
            return null;
        }
    }

    /**
     * Получает список идентификаторов сотрудников с заданным статусом.
     *
     * @param status           статус сотрудника
     * @param userTelegramName имя пользователя в Telegram
     * @return список идентификаторов сотрудников или null, если произошла ошибка
     */
    public List<Long> getEmployeeListWithStatus(EmployeeStatus status, String userTelegramName) {
        HttpHeaders headers = createHeaders(userTelegramName);
        try {
            ResponseEntity<Long[]> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/status/" + status),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
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

    /**
     * Возвращает список сотрудников и их статусов.
     *
     * @param userTelegramName имя пользователя в Telegram
     * @return словарь, где ключ - идентификатор сотрудника, значение - статус сотрудника
     */
    public Map<Long, EmployeeStatus> getListEmployeeAndStatus(String userTelegramName) {

        HttpHeaders headers = createHeaders(userTelegramName);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map<Long, EmployeeStatus>> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/status"),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<Long, EmployeeStatus>>() {
                    }
            );
            Map<Long, EmployeeStatus> employeeStatusMap = null;
            if (response.getBody() != null) {
                employeeStatusMap = response.getBody();
            }
            return employeeStatusMap;
        } catch (Exception e) {
            log.error("" + e);
            return null;
        }
    }

    /**
     * Получает информацию об администраторе.
     *
     * @return объект EmployeeResponse с информацией об администраторе
     */
    public EmployeeResponse getAdminInfo() {
        HttpHeaders headers = createHeaders(integrationConfig.getAdminLogin());
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
     * @param telegramName     имя в Telegram
     * @param userFIO          ФИО пользователя
     * @param userTelegramName имя пользователя в Telegram для авторизации
     * @return объект EmployeeResponse с информацией о созданном сотруднике
     */
    public EmployeeResponse createEmployee(String telegramName, String userFIO, String userTelegramName) {
        HttpHeaders headers = createHeaders(userTelegramName);

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(telegramName);
        userRequest.setFio(userFIO);

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

    /**
     * Удаляет сотрудника по его имени в Telegram.
     *
     * @param telegramName      имя пользователя в Telegram
     * @param userTelegramName  имя пользователя в Telegram, от имени которого выполняется запрос
     * @return сообщение об успешном удалении или ошибке при удалении сотрудника
     */
    public String deleteEmployee(String telegramName, String userTelegramName) {
        HttpHeaders headers = createHeaders(userTelegramName);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    String.format(integrationConfig.getEmployeeUrl(), integrationConfig.getGetSuffixEmployee() + "/" + telegramName),
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return "Сотрудник успешно удален.";
            } else {
                return "Сотрудник не удален. Повторите запрос: " + response.getBody();
            }
        } catch (Exception e) {
            log.error("Ошибка: " + e.getMessage());
            return "Ошибка при удалении сотрудника: " + e.getMessage();
        }
    }

}
