package org.example;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import org.example.dao.entities.Vacation;
import org.example.dao.repositories.VacationRepository;
import org.example.dto.VacationRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;


@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {

    private static final Long ADMIN_ID = 1L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    VacationRepository repo;

    @Test
    @Order(1)

    public void getOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/telegram/vacation?employeeId=" + ADMIN_ID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void postOk() throws Exception {
        VacationRequest request = new VacationRequest();
        request.setEmployeeId(ADMIN_ID);
        //нарочно взял такие отпуска в грядущем чтобы потом их удалить
        request.setStartDate(LocalDate.parse("2028-09-20"));
        request.setEndDate(LocalDate.parse("2028-09-22"));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/telegram/vacation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void cancelOk() throws Exception {
        //ищу отпуска в грядущем и удаляю
        List<Vacation> lst =
                repo.findByEmployeeIdAndStartDateAfter(ADMIN_ID, LocalDate.parse("2028-09-01"));
        if (lst == null || lst.isEmpty()) {
            throw new RuntimeException("Тесты должны запускаться полным пакетом");
        }
        Long id = lst.get(0).getId();
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/telegram/vacation/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

}
