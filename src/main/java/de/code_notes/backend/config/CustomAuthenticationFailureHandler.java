package de.code_notes.backend.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Class handling login failure. Implements {@link AuthenticationFailureHandler} and is beeing used in {@link SecurityConfig}
 * 
 * @since 0.0.1
 */
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request, 
        HttpServletResponse response, 
        AuthenticationException exception) throws IOException, ServletException {

        response.setStatus(401);
    }
}