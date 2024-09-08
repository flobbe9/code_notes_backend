package de.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.services.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;


/**
 * @since 0.0.1
 */
@RestController
@RequestMapping("/appUser")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    
    /**
     * Save new or update given {@code appUser}. Pass {@code appUser} id to update, dont pass it to save {@code appUser} as new entry.
     * 
     * @param appUser
     * @return
     */
    @PostMapping("/save")
    @Operation(
        description = "Save (set id null) or update (pass valid id)", 
        responses = {
            @ApiResponse(responseCode = "200", description = "AppUser saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid appUser"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
            @ApiResponse(responseCode = "406", description = "Invalid appUser id"),
            @ApiResponse(responseCode = "409,", description = "AppUser's email in conflict with existing ones"),
            @ApiResponse(responseCode = "500", description = "AppUser null")
        }
    )
    public Mono<AppUser> save(@RequestBody @Valid AppUser appUser) {

        return Mono.just(this.appUserService.save(appUser));
    }


    @DeleteMapping("/delete")
    @Operation(
        description = "Delete", 
        responses = {
            @ApiResponse(responseCode = "200", description = "AppUser deleted or did not exist anyway"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf")
        }
    )
    public void delete(@RequestParam Long id) {

        this.appUserService.delete(id);
    }
}