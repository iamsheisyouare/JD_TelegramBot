package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.EmplRequest;
import org.example.dto.EmployeeResponse;
import org.example.jwt.JwtCreator;
import org.example.jwt.dto.JwtRequest;
import org.example.jwt.dto.JwtResponse;
import org.example.model.Employee;
import org.example.repository.EmployeeRepository;
import org.example.service.AuthenticatedUserService;
import org.example.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/*
В НАСТОЯШИЙ МОМЕНТ КОНТРОЛЛЕР НЕ ИСПОЛЬЗУЕТСЯ


@RestController
//@RequestMapping("empl/auth")
@RequestMapping("empl")
@RequiredArgsConstructor
public class AuthController {

    private final JwtCreator jwtCreator;
    private final EmployeeService service;
    private final AuthenticatedUserService authenticatedUserService;




    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("auth")
    public ResponseEntity<JwtResponse> changeJwt(@RequestBody JwtRequest authRequest) {
        return service.changeJwt(authRequest,jwtCreator);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("new")
    public ResponseEntity<Employee> create(@RequestBody EmplRequest request) {
        return service.createEmpl(request,jwtCreator);
    }


    @PreAuthorize("@authenticatedUserService.hasId(#id) or hasRole('ROLE_ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<Employee> findById(@PathVariable Long id)
    {
        return service.findById(id);
    }

    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("@authenticatedUserService.hasName(#name) or hasRole('ROLE_ADMIN')")
    @GetMapping("/name/{name}")
    public ResponseEntity<EmployeeResponse> findByName(@PathVariable String name) {return service.findByName(name);}

}
 */