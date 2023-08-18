package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.jwt.JwtCreator;
import org.example.jwt.dto.JwtRequest;
import org.example.jwt.dto.JwtResponse;
import org.example.model.Employee;
import org.example.repository.EmployeeRepository;
import org.example.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtCreator jwtCreator;
    private final EmployeeService service;
    private final EmployeeRepository repository;

    @PostMapping//("login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest authRequest) {
        Employee employee = service.getByName(authRequest.getUsername()).get();
        employee.setPassword(authRequest.getPassword());
        final JwtResponse token = jwtCreator.createJwt(authRequest);

        employee.setToken(token.getAccessToken());
        repository.save(employee);
        return ResponseEntity.ok(token);
    }
}
