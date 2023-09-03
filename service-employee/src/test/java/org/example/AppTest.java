package org.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.example.jwt.dto.JwtRequest;
import org.example.model.Employee;
import org.example.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
//@Import(SecurityConfig.class)
public class AppTest {

    private static final Long ADMIN_ID = 1L;
    private static final Long USER_ID = 2L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeRepository employeeRepository;


    /**
     * Админ обладает расширенными правами на ресурсе employee
     *
     * @throws Exception
     */
    @Test
    @Transactional
    public void testAdminRightsOnSecurable() throws Exception {

        String token = null;
        Optional<Employee> emp = employeeRepository.findById(ADMIN_ID);
        if (emp.isPresent()) {
            token = emp.get().getToken();
        } else {
            throw new RuntimeException("Пользователя не нашли");
        }

        //показываем что это админ
        assertTrue(isAdmin(emp.get()));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/employee/{id}", ADMIN_ID)
                        .header("authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    /**
     * Пользователь на защещенном ресурсе может смотреть данные только по себе
     *
     * @throws Exception
     */
    @Test
    @Transactional
    public void testSuccessUserRightsOnSecurable() throws Exception {

        String token = null;
        Optional<Employee> emp = employeeRepository.findById(USER_ID);
        if (emp.isPresent()) {
            token = emp.get().getToken();
        } else {
            throw new RuntimeException("Пользователя не нашли");
        }

        //показываем что это не админ
        assertFalse(isAdmin(emp.get()));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/employee/{id}", USER_ID)
                        .header("authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


    }

    /**
     * При попытке посмотреть пользователю не свою запись на защищенном ресурсе возникает 403 ошибка
     *
     * @throws Exception
     */
    @Test
    @Transactional
    public void testForbiddenUserRightsOnSecurable() throws Exception {

        String token = null;
        Optional<Employee> emp = employeeRepository.findById(USER_ID);
        if (emp.isPresent()) {
            token = emp.get().getToken();
        } else {
            throw new RuntimeException("Пользователя не нашли");
        }

        //показываем что это не админ
        assertFalse(isAdmin(emp.get()));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/employee/{id}", ADMIN_ID)
                        .header("authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * На незащещенный ресурс можно попадать без аутентфикации
     *
     * @throws Exception
     */
    @Test
    public void testOnNonSecurable() throws Exception {
        Optional<Employee> emp = employeeRepository.findById(USER_ID);
        if (!emp.isPresent()) {
            throw new RuntimeException("Пользователя не нашли");
        }
        JwtRequest req = new JwtRequest();
        req.setUsername(emp.get().getTelegramName());
        req.setPassword(emp.get().getPassword());

        String name = emp.get().getTelegramName();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(req);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    private boolean isAdmin(Employee empl) {
        return (empl.getRoles().stream().
                filter(r -> r.getRoleName().
                        equals("ROLE_ADMIN")).findFirst().isPresent());
    }

}
