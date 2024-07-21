package de.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.services.AppUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;


/**
 * @since 0.0.1
 */
@RestController
@RequestMapping("/appUser")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    
    @PostMapping("/save")
    public Mono<AppUser> save(@RequestBody @Valid @NotNull(message = "'appUser' cannot be null") AppUser appUser) {

        return Mono.just(this.appUserService.save(appUser));
    }
}