package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.EmployeeResponse;
import org.example.jwt.JwtCreator;
import org.example.jwt.dto.JwtRequest;
import org.example.jwt.dto.JwtResponse;
import org.example.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final JwtCreator jwtCreator;
    private final EmployeeService service;


    @PostMapping("login")
    public ResponseEntity<EmployeeResponse> login(@RequestBody JwtRequest authRequest) {
        return service.login(authRequest,jwtCreator);
    }
}
