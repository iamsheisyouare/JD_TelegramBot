package org.example.controller;
/*
import lombok.RequiredArgsConstructor;
import org.example.jwt.JwtCreator;
import org.example.model.jwt.JwtAuthentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

В НАСТОЯШИЙ МОМЕНТ КОНТРОЛЛЕР НЕ ИСПОЛЬЗУЕТСЯ

@RestController
@RequestMapping("empl")
@RequiredArgsConstructor
public class UserController {

    private final JwtCreator authService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("hello/user")
    public ResponseEntity<String> helloUser() {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        return ResponseEntity.ok("Hello user " + authInfo.getPrincipal() + "!");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("hello/admin")
    public ResponseEntity<String> helloAdmin() {
        final JwtAuthentication authInfo = authService.getAuthInfo();
        return ResponseEntity.ok("Hello admin " + authInfo.getPrincipal() + "!");
    }

}

 */
