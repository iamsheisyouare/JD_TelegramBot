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

    @SneakyThrows
    public JwtResponse createJwt(JwtRequest request) {
        final Employee employee  = employeeService.getByName(request.getUsername())
                .orElseThrow(() -> new AuthException("Пользователь не найден"));
        if (!employee.getPassword().equals(request.getPassword())) {
            throw new AuthException("Неправильный пароль");
        }
        final String accessToken = jwtProvider.generateAccessToken(employee);
        return new JwtResponse(accessToken, null, request.getUsername());

    }
    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
