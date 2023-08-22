package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.Employee;
import org.example.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {
    //@Autowired
    private final EmployeeRepository repository;

    public boolean hasId(Long id){
        String contextName =  SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Employee employee = repository.findByTelegramName(contextName).get();
        return employee.getId().equals(id);

    }
    public boolean hasName(String name){
        String contextName =  SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return contextName.equals(name);

    }
}
