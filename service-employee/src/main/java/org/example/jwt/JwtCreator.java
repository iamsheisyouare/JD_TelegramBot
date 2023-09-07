package org.example.jwt;

import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.example.jwt.dto.JwtRequest;
import org.example.jwt.dto.JwtResponse;
import org.example.model.Employee;
import org.example.model.jwt.JwtAuthentication;
import org.example.service.EmployeeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Класс который создает jwt
 */
@Service
@AllArgsConstructor

public class JwtCreator {

    private final EmployeeService employeeService;
    private final JwtProvider jwtProvider;

    /**
     * метод при режиме получения нового jwt у существующего сотрудника
     *
     * @param request
     * @return
     */
    @SneakyThrows
    public JwtResponse createJwt(JwtRequest request) {
        final Employee employee = employeeService.getByName(request.getUsername())
                .orElseThrow(() -> new AuthException("Пользователь не найден"));
        if (!employee.getPassword().equals(request.getPassword())) {
            throw new AuthException("Неправильный пароль");
        }
        final String accessToken = jwtProvider.generateAccessToken(employee);
        return new JwtResponse(accessToken, request.getUsername());


    }

    /**
     * метод при создании нового сотрудника (пока не занесен в БД)
     *
     * @param employee сотрудник
     * @return строка jwt
     */
    public String createJwt(Employee employee) {
        return jwtProvider.generateAccessToken(employee);

    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
