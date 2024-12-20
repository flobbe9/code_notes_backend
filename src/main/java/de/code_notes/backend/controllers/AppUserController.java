package de.code_notes.backend.controllers;

import static de.code_notes.backend.helpers.Utils.LOGIN_PATH;
import static de.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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
import de.code_notes.backend.services.AsyncService;
import de.code_notes.backend.services.DeletedEntityRecordService;
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
public class AppUserController extends DeletedEntityRecordController {

    /** The url query param key that is appended to the redirect url after account confirmation. Also hard coded in "constatns.ts" */
    private static final String CONFIRM_ACCOUNT_STATUS_PARAM = "confirm-account-status-code";
    /** The url query param key that is appended to the redirect url after requesting a reset-password mail externally. Also hard coded in "constatns.ts" */
    private static final String SEND_RESET_PASSWORD_MAIL_STATUS_PARAM = "send-reset-password-mail";

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private DeletedEntityRecordService deletedEntityRecordService;


    @PostMapping("/save")
    @Operation(
        description = "Save new (id null) or update (valid id). AuthRequirements: LOGGED_IN", 
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
            @ApiResponse(responseCode = "400", description = "Invalid params"),
            @ApiResponse(responseCode = "406", description = "Invalid param pattern"),
            @ApiResponse(responseCode = "409", description = "App user with this email already exists"),
            @ApiResponse(responseCode = "500", description = "Any other error"),
        }
    )
    public void register(
        @RequestParam @NotBlank(message = "'email' cannot be blank") String email, 
        @RequestParam @NotBlank(message = "'password' cannot be blank") String password) throws ResponseStatusException, IllegalArgumentException, MessagingException, IllegalStateException, IOException {
        
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
    public void resendConfirmationMail(@RequestParam @NotBlank(message = "'email' cannot be blank") String email) throws IllegalArgumentException, ResponseStatusException, MessagingException, IllegalStateException, IOException {

        this.appUserService.resendAccountRegistrationConfirmationMail(email);
    }


    @GetMapping("/confirm-account")
    @Operation(
        description = 
            "Confirm both the confirmation token and the app user linked to given token value. " +
            "Will redirect to frontend login url and append the respnose status code (successful or not) like this: " + 
            "http://localhost:3000/login/?" + CONFIRM_ACCOUNT_STATUS_PARAM + "={statusCode}",
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

            redirectUrl += "/?%s=200".formatted(CONFIRM_ACCOUNT_STATUS_PARAM);

        } catch (ResponseStatusException e) {
            redirectUrl += "/?%s=%s".formatted(CONFIRM_ACCOUNT_STATUS_PARAM, e.getStatusCode().value());
            CustomExceptionHandler.logPackageStackTrace(e, e.getReason());

        } catch (Exception e) {
            redirectUrl += "/?%s=500".formatted(CONFIRM_ACCOUNT_STATUS_PARAM);
            CustomExceptionHandler.logPackageStackTrace(e);
        }

        Utils.redirect(response, redirectUrl);
    }


    @DeleteMapping("/delete-current")
    @Operation(
        description = "Delete app user currently logged in, send a mail notice and logout. Will save a deletion-record to db. AuthRequirements: LOGGED_IN", 
        responses = {
            @ApiResponse(responseCode = "200", description = "AppUser deleted (if existed) and logged out"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf")
        }
    )
    // dont return anything here, since 401 would be thrown after logout call
    public void deleteCurrent(HttpServletResponse response) throws JsonProcessingException, IllegalArgumentException, IOException, IllegalStateException, MessagingException {

        AppUser currentAppUser = this.appUserService.getCurrent();

        this.appUserService.deleteCurrent();

        try {
            this.deletedEntityRecordService.saveFor(currentAppUser);
            
            this.asyncService.sendAppUserHasBeenDeletedMail(currentAppUser.getEmail());
            
            Utils.writeToResponse(response, OK, "Deleted current user and logged out");

        } catch (Exception e) {
            throw e;

        } finally {
            this.appUserService.logout();
        }
    }


    @GetMapping("/check-logged-in")
    @Operation(
        description = "Indicates whether the current session is still valid or not. AuthRequirements: NONE",
        responses = {
            @ApiResponse(responseCode = "200", description = "In any case. Returns true if is logged in, else false"),
        }
    )
    public Mono<Boolean> checkLoggedIn(Authentication authentication) {

        return Mono.just(authentication != null);
    }


    @PostMapping("/get-current")
    @Operation(
        description = "Get the app user currently logged in from db. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returned the app user of the current session"),
            @ApiResponse(responseCode = "401", description = "Current session is invalid, user is not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
            @ApiResponse(responseCode = "404", description = "Current app user not found in db"),
            @ApiResponse(responseCode = "501", description = "Current principal is neither normally logged in nor an oauth2 user"),
        }
    )
    public Mono<AppUser> getCurrent(HttpServletRequest request) {

        return Mono.just(this.appUserService.loadCurrentFromDb());
    }


    @PostMapping("/reset-password")
    @Operation(
        description = "Reset password via account settings when logged in. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Password reset, appUser saved"),
            @ApiResponse(responseCode = "400", description = "Invalid args or new password does not match pattern. Make sure that one of the optional params is present"),
            @ApiResponse(responseCode = "401", description = "Current session is invalid, user is not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
            @ApiResponse(responseCode = "406", description = "Old password does not match current one"),
            @ApiResponse(responseCode = "409", description = "appUser not enabled"),
            @ApiResponse(responseCode = "417", description = "appUser does not have a password in the first place (propably oauth2 user)"),
            @ApiResponse(responseCode = "500", description = "Any other unexpected error")
        }
    )
    public void resetPassword(@RequestParam @NotBlank(message = "'newPassword' cannot be blank") String newPassword, @RequestParam @NotBlank(message = "'oldPassword' cannot be blank") String oldPassword) throws IllegalArgumentException, ResponseStatusException, IllegalStateException, IOException, MessagingException {

        this.appUserService.resetPassword(newPassword, oldPassword);
    }

    
    @PostMapping("/reset-password-by-token")
    @Operation(
        description = "Reset password by 'password-reset-mail'",
        responses = {
            @ApiResponse(responseCode = "200", description = "Password reset, appUser saved"),
            @ApiResponse(responseCode = "400", description = "Invalid args or new password does not match pattern. Make sure that one of the optional params is present"),
            @ApiResponse(responseCode = "404", description = "'token' not found in db"),
            @ApiResponse(responseCode = "409", description = "appUser not enabled"),
            @ApiResponse(responseCode = "417", description = "appUser does not have a password in the first place (propably oauth2 user)"),
            @ApiResponse(responseCode = "500", description = "Any other unexpected error")
        }
    )
    public void resetPasswordByToken(@RequestParam @NotBlank(message = "'newPassword' cannot be blank") String newPassword, @RequestParam @NotBlank(message = "'token' cannot be blank") String token) throws IllegalArgumentException, ResponseStatusException, IllegalStateException, IOException, MessagingException {

        this.appUserService.resetPasswordByToken(newPassword, token);
    }

    
    @GetMapping("/send-reset-password-mail")
    @Operation(
        description = 
        """
            Send mail to given 'to' user to reset their password. Optionally redirect to frontend /login appending the error status code, or, if redirect url is ommited,
            return the error object.
        """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Mail has been sent, user is valid candidate for reset-password mail"),
            @ApiResponse(responseCode = "400", description = "Invalid args"),
            @ApiResponse(responseCode = "404", description = "No app user with that email"),
            @ApiResponse(responseCode = "409", description = "App user not enabled yet"),
            @ApiResponse(responseCode = "417", description = "App user without password (propably oauth2 user)"),
            @ApiResponse(responseCode = "500", description = "Any other unexpected error")
        }
    )
    public void sendResetPasswordMail(@RequestParam @NotBlank(message = "'to' cannot be blank") String to, @RequestParam Optional<String> redirectTo, HttpServletResponse response) throws Exception {

        String redirectUrl = redirectTo.orElse("");
        Exception exception = null;

        try {
            this.appUserService.sendResetPasswordMail(to);
            redirectUrl += "/?%s=200".formatted(SEND_RESET_PASSWORD_MAIL_STATUS_PARAM);

        } catch (ResponseStatusException e) {
            redirectUrl += "/?%s=%s".formatted(SEND_RESET_PASSWORD_MAIL_STATUS_PARAM, e.getStatusCode().value());
            exception = e;

        } catch (Exception e) {
            redirectUrl += "/?%s=500".formatted(SEND_RESET_PASSWORD_MAIL_STATUS_PARAM);
            exception = e;
        }

        if (redirectTo.isPresent()) {
            Utils.redirect(response, redirectUrl);

            if (exception != null)
                CustomExceptionHandler.logPackageStackTrace(exception);

        } else if (exception != null) {
            throw exception;
        }
    }


    @Override
    protected String getDeletedEntityClassName() {

        return new AppUser().getDeletedEntityClassName();
    }
}