package de.code_notes.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@RestController
@RequestMapping("/test")
public class TestController {
    
    @PostMapping("")
    public void test(@RequestParam @Min(value = 3, message = "lower than 3") Integer num) {
        
        throw new ResponseStatusException(HttpStatus.OK, num.toString());
    }
}