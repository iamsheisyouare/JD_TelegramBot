package org.example.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.dto.EmplRequest;
import org.example.dto.EmployeeResponse;
import org.example.enums.EmployeeStatus;
import org.example.jwt.JwtCreator;
import org.example.jwt.dto.JwtRequest;
import org.example.jwt.dto.JwtResponse;
import org.example.model.Employee;
import org.example.model.Role;
import org.example.repository.EmployeeRepository;
import org.example.repository.RoleRepository;
import org.springframework.http.HttpStatus;
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
     *
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
    public ResponseEntity<EmployeeResponse> createEmpl(EmplRequest request, JwtCreator jwtCreator) {
        try {

            String password = UUID.randomUUID().toString().replace("-", "");
            Employee employee = new Employee(request.getFio(), request.getUsername(), password);
            Role userRole = roleRepository.findByRoleName("ROLE_USER").orElse(null);
            employee.setRoles(List.of(userRole));
            employee.setToken(jwtCreator.createJwt(employee));
            Employee saved = employeeRepository.save(employee);
            return ResponseEntity.ok(
                    new EmployeeResponse(saved.getTelegramName(), saved.getId(), saved.getToken(), saved.getStatus(),
                            saved.getFio()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    public ResponseEntity<EmployeeResponse> findById(Long id) {
        try {
            Employee empl = employeeRepository.findById(id).orElse(null);
            if (empl == null) {
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(null);
            }
            return ResponseEntity.ok(
                    new EmployeeResponse(empl.getTelegramName(), empl.getId(), empl.getToken(), empl.getStatus(),
                            empl.getFio()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    public ResponseEntity<EmployeeResponse> findByName(String name) {
        try {
            Employee empl = employeeRepository.findByTelegramName(name).orElse(null);
            if (empl == null) {
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(null);
            }
            return ResponseEntity.ok(
                    new EmployeeResponse(empl.getTelegramName(), empl.getId(), empl.getToken(), empl.getStatus(),
                            empl.getFio()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Пользователь логируется в системе
     *
     * @param authRequest
     * @param jwtCreator
     * @return response
     */
    //public ResponseEntity<JwtResponse> login(JwtRequest authRequest, JwtCreator jwtCreator) {
    public ResponseEntity<EmployeeResponse> login(JwtRequest authRequest, JwtCreator jwtCreator) {
        try {
            Employee employee = getByName(authRequest.getUsername()).get();
            final JwtResponse token = jwtCreator.createJwt(authRequest);

            employee.setToken(token.getAccessToken());
            Employee saved = employeeRepository.save(employee);
            return ResponseEntity.ok(
                    new EmployeeResponse(saved.getTelegramName(), saved.getId(), saved.getToken(), saved.getStatus(),
                            saved.getFio()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Нахождение списка сотрудников по статусу
     *
     * @param status
     * @return список id сотрудников
     */

    public ResponseEntity<List<Long>> findByStatus(EmployeeStatus status) {
        try {
            List<Employee> employees = employeeRepository.findByStatus(status);
            if (employees == null || employees.isEmpty()) {
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(null);
            }
            return ResponseEntity.ok(employees.stream().map(e -> e.getId()).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }


    /**
     * Список всех сотрудников со стаусами
     *
     * @return Map<id сотрудника, статус>
     */
    public ResponseEntity<Map<Long, EmployeeStatus>> findAllWithStatus() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            if (employees.isEmpty()) {
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(null);
            }
            return ResponseEntity.ok(employees.stream()
                    .collect(Collectors.toMap(Employee::getId, Employee::getStatus)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    /**
     * Увольнение / восстановление сотрудника
     * @param name телегррам имя сотрудника
     * @param isDeleted true удалить/false восстановить
     * @return
     */
    public ResponseEntity<String> deleteOrRestoreEmpl(String name, boolean isDeleted) {
        try {
            String action = (isDeleted ? "уволен" : "восстановлен");
            String message = "Сотрудник \"" + name + "\" ";
            Employee employee = employeeRepository.findByTelegramName(name).orElse(null);
            if (employee == null) {
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(message + "не найден");
            }

            if (isDeleted) {
                if (employee.getStatus() == EmployeeStatus.FIRED) {
                    return ResponseEntity.status(HttpStatusCode.valueOf(406)).body(message + "уже был уволен");
                }
                employee.setStatus(EmployeeStatus.FIRED);
            }
            else
            {
                if (employee.getStatus() == EmployeeStatus.WORK) {
                    return ResponseEntity.status(HttpStatusCode.valueOf(406)).body(message + "в данный момент работает");
                }
                employee.setStatus(EmployeeStatus.WORK);

            }

            Employee saved = employeeRepository.save(employee);
            return ResponseEntity.ok(message + action);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
