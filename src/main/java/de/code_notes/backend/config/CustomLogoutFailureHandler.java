package de.code_notes.backend.config;

import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
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
public class CustomLogoutFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request, 
        HttpServletResponse response, 
        AuthenticationException exception) throws IOException, ServletException {

        int status = 401;
        response.setStatus(status);

        response.getWriter().write(Utils.getDefaultObjectMapper().writeValueAsString(
            new CustomExceptionFormat(status, "Login failed")
        ));
    }
}