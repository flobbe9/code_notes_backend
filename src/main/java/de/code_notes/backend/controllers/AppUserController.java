package de.code_notes.backend.controllers;

import static de.code_notes.backend.helpers.Utils.LOGIN_PATH;
import static de.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.helpers.Utils;
import de.code_notes.backend.services.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import reactor.core.publisher.Mono;


/**
 * @since 0.0.1
 */
@RestController
@RequestMapping("/app-user")

public class AppUserController {

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;

    @Autowired
    private AppUserService appUserService;

    
    @PostMapping("/save")
    @Operation(
        description = "Save new (set id null) or update (pass valid id). AuthRequirements: LOGGED_IN", 
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


    @PostMapping("/register")
    @Operation(
        description = "Saves new app user and sends a confirmation mail (asynchronously). AuthRequirements: NONE",
        responses = {
            @ApiResponse(responseCode = "200", description = "New user saved, mail sending process has been started"),
            @ApiResponse(responseCode = "202", description = "App user already enabled"),
            @ApiResponse(responseCode = "400", description = "Invalid params"),
            @ApiResponse(responseCode = "406", description = "Invalid param pattern"),
            @ApiResponse(responseCode = "409", description = "App user with this email already exists"),
            @ApiResponse(responseCode = "500", description = "Any other error"),
        }
    )
    public void register(
        @RequestParam @NotBlank(message = "'email' cannot be blank") String email, 
        @RequestParam @NotBlank(message = "'password' cannot be blank") String password) throws ResponseStatusException, IllegalArgumentException, MessagingException {
        
        this.appUserService.register(email, password);
    }


    @GetMapping("/resend-confirmation-mail")
    @Operation(
        description = "Resend confirmation mail if appUser with given email exists and is not enabled yet",
        responses = {
            @ApiResponse(responseCode = "200", description = "App user exists, mail sending process has been started again"),
            @ApiResponse(responseCode = "202", description = "App user already enabled"),
            @ApiResponse(responseCode = "400", description = "Invalid params"),
            @ApiResponse(responseCode = "404", description = "No app user with given email"),
            @ApiResponse(responseCode = "500", description = "Any other error"),
        }
    )
    public void resendConfirmationMail(@RequestParam @NotBlank(message = "'email' cannot be blank") String email) throws IllegalArgumentException, ResponseStatusException, MessagingException {

        this.appUserService.resendAccountRegistrationConfirmationMail(email);
    }


    @GetMapping("/confirm-account")
    @Operation(
        description = 
            """
                Confirm both the confirmation token and the app user linked to given token value.
                Will in any case redirect to frontend login url and (if error) append the respnose status code like this: http://localhost:3000/login/?error-status={statusCode}          
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Confirmed token and app user"),
            @ApiResponse(responseCode = "202", description = "App user or token confirmed already"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token param"),
            @ApiResponse(responseCode = "404", description = "No confirmation token with given token value"),
            @ApiResponse(responseCode = "406", description = "Token expired"),
            @ApiResponse(responseCode = "500", description = "Any other error"),
        }
    )
    public void confirmAccount(@RequestParam Optional<String> token, HttpServletResponse response) throws IOException {

        String redirectUrl = this.FRONTEND_BASE_URL + LOGIN_PATH;

        try {
            if (isBlank(token.orElse(null)))
                throw new ResponseStatusException(BAD_REQUEST, "Missing 'token' param or it's value");

            this.appUserService.confirmAppUser(token.get());

        // possibly append a bad status code and log
        } catch (ResponseStatusException e) {
            if (!Utils.isHttpStatusAlright(e.getStatusCode().value()))
                redirectUrl += "/?error-status=" + e.getStatusCode().value();

            CustomExceptionHandler.logPackageStackTrace(e, e.getReason());

        } catch (Exception e) {
            redirectUrl += "/?error-status=500";
            CustomExceptionHandler.logPackageStackTrace(e);
        }

        Utils.redirect(response, redirectUrl);
    }



    @DeleteMapping("/delete-current")
    @Operation(
        description = "Delete app user currently logged in and logout. AuthRequirements: LOGGED_IN", 
        responses = {
            @ApiResponse(responseCode = "200", description = "AppUser deleted (if existed) and logged out"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf")
        }
    )
    // dont return anything here, since 401 would be thrown afeter logout call
    public void deleteCurrent(HttpServletResponse response) throws JsonProcessingException, IllegalArgumentException, IOException {

        this.appUserService.deleteCurrent();

        Utils.writeToResponse(response, OK, "Deleted current user and logged out");

        this.appUserService.logout();
    }


    @GetMapping("/check-logged-in")
    @Operation(
        description = "Indicates whether the current session is still valid or not. AuthRequirements: NONE",
        responses = {
            @ApiResponse(responseCode = "200", description = "Current session is valid, user is logged in"),
            @ApiResponse(responseCode = "401", description = "Current session is invalid, user is not logged in")
        }
    )
    public Mono<ResponseEntity<CustomExceptionFormat>> checkLoggedIn() {

        return SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null ?
            Mono.just(CustomExceptionHandler.getResponse(HttpStatus.UNAUTHORIZED))
            :
            Mono.just(CustomExceptionHandler.getResponse(HttpStatus.OK, "Logged in"));
    }


    @PostMapping("/get-current")
    @Operation(
        description = "Get the app user currently logged in from db. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returned the app user of the current session"),
            @ApiResponse(responseCode = "401", description = "Current session is invalid, user is not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
            @ApiResponse(responseCode = "404", description = "Current app user not found in db")
        }
    )
    public Mono<AppUser> getCurrent(HttpServletRequest request) {

        return Mono.just(this.appUserService.getCurrentFromDb());
    }
}