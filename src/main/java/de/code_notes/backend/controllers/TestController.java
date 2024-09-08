package de.code_notes.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.constraints.Min;


@RestController
@RequestMapping("/test")
public class TestController {
    
    @PostMapping
    @Secured("ROLE_ADMIN")
    public void test(@RequestParam @Min(value = 3, message = "lower than 3") Integer num) {
        
        throw new ResponseStatusException(HttpStatus.OK, num.toString());
    }

        
    @GetMapping
    public String test() {

        return "test";
    }
}