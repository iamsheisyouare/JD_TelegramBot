package org.example.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.dto.EmplRequest;
import org.example.dto.EmployeeResponse;
import org.example.jwt.JwtCreator;
import org.example.jwt.dto.JwtRequest;
import org.example.jwt.dto.JwtResponse;
import org.example.model.Employee;
import org.example.model.Role;
import org.example.repository.EmployeeRepository;
import org.example.repository.RoleRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    public Optional<Employee> getByName(@NonNull String username) {
        return employeeRepository.findByTelegramName(username);
    }

    /**
     * Смена jwt
     * @param authRequest
     * @param jwtCreator
     * @return
     */
    public ResponseEntity<JwtResponse> changeJwt(JwtRequest authRequest, JwtCreator jwtCreator) {
        Employee employee = getByName(authRequest.getUsername()).get();
        employee.setPassword(authRequest.getPassword());
        final JwtResponse token = jwtCreator.createJwt(authRequest);

        employee.setToken(token.getAccessToken());
        employeeRepository.save(employee);
        return ResponseEntity.ok(token);
    }

    /**
     * Создание нового пользователя. По умолчанию присваивается ROLE_USER
     *
     * @param request
     * @param jwtCreator
     * @return
     */
    public ResponseEntity<Employee> createEmpl(EmplRequest request, JwtCreator jwtCreator) {
        String password = UUID.randomUUID().toString().replace("-", "");
        Employee employee = new Employee(request.getFio(), request.getUsername(), password);
        Role userRole = roleRepository.findByRoleName("ROLE_USER").orElse(null);
        employee.setRoles(List.of(userRole));
        employee.setToken(jwtCreator.createJwt(employee));
        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.ok(saved);
    }

    public ResponseEntity<Employee> findById(Long id)
    {
        return ResponseEntity.ok(employeeRepository.findById(id).orElse(null));
    }

    public ResponseEntity<EmployeeResponse> findByName(String name)
    {
        Employee empl= employeeRepository.findByTelegramName(name).orElse(null);
        if (empl == null) return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(null);
        return ResponseEntity.ok(new EmployeeResponse(empl.getFio(),empl.getId(),empl.getToken()));
    }

}
