package org.example.dev;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jwt.JwtCreator;
import org.example.model.Employee;
import org.example.model.Role;
import org.example.repository.EmployeeRepository;
import org.example.repository.RoleRepository;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class InitTestData {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtCreator jwtCreator;


    @PostConstruct
    public void handleStartedEvent() {
        employeeRepository.deleteAll();
        roleRepository.deleteAll();

        log.info("Initiating test data ...");
        Role adminRole = new Role();
        adminRole.setRoleName(ROLE_ADMIN);
        roleRepository.save(adminRole);
        log.info("role added '{}'", adminRole);

        Role userRole = new Role();
        userRole.setRoleName(ROLE_USER);
        roleRepository.save(userRole);

        var empl = new Employee("Vasay Pupkin","oduvan","password");
        empl.setRoles(List.of(adminRole,userRole));
        empl.setToken(jwtCreator.createJwt(empl));
        employeeRepository.save(empl);
        log.info("user added '{}'", empl);
    }

}
