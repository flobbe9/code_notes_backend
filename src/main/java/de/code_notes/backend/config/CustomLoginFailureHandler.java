package de.code_notes.backend.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import de.code_notes.backend.controllers.CustomExceptionFormat;
import de.code_notes.backend.helpers.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Class handling login failure. Implements {@link AuthenticationFailureHandler} and is beeing used in {@link SecurityConfig}. <p>
 * 
 * Will return 401 status with a {@link CustomExceptionFormat} object as body.
 * 
 * @since 0.0.1
 */
@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        int status = 401;
        response.setStatus(status);

        Utils.writeToResponse(response, HttpStatus.valueOf(status), "Login failed");
    }
}