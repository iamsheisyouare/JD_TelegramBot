package ru.sberbank.jd.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.jd.dto.EmployeeResponse;

@Slf4j
@Component
public class EmployeeApiHandler {

    private final RestTemplate restTemplate;

    @Autowired
    public EmployeeApiHandler(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

    public EmployeeResponse getEmployeeByTelegramName(String telegramName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJvZHV2YW4iLCJleHAiOjE2OTM0MjQ2NTYsInJvbGVzIjpbIlJPTEVfQURNSU4iLCJST0xFX1VTRVIiXSwiZnVsbE5hbWUiOiJWYXNheSBQdXBraW4ifQ.XxuI2rEcz6tVxqf7pQRmUz6_ezCUDJaeF6wv6MapRNxRD0ewOuvat7EZwHeo5cYHiMBHoFzbhKIqZz9JEVoNTQ");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
                    //String.format(apiUri, getSuffix + "/" + "oduvan"),
                    "http://localhost:8001/empl/name/oduvan",
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
}
