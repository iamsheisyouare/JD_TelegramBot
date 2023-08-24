package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.EmplRequest;
import org.example.dto.EmployeeResponse;
import org.example.jwt.JwtCreator;
import org.example.model.Employee;
import org.example.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final JwtCreator jwtCreator;
    private final EmployeeService service;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody EmplRequest request) {
        return service.createEmpl(request,jwtCreator);
    }

    @PreAuthorize("@authenticatedUserService.hasId(#id) or hasRole('ROLE_ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<Employee> findById(@PathVariable Long id)
    {
        return service.findById(id);
    }

    @PreAuthorize("@authenticatedUserService.hasName(#name) or hasRole('ROLE_ADMIN')")
    @GetMapping("/name/{name}")
    public ResponseEntity<EmployeeResponse> findByName(@PathVariable String name)
    {return service.findByName(name);}
}