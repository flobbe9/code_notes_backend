package de.code_notes.backend.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import de.code_notes.backend.controllers.CustomExceptionFormat;
import de.code_notes.backend.helpers.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Return a {@code CustomExceptionFormat} with status 401. Implements {@link AuthenticationEntryPoint}.
 * 
 * @since 0.0.1
 */
public class CustomUnAuthenticatedHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.getWriter().write(Utils.getDefaultObjectMapper().writeValueAsString(
            new CustomExceptionFormat(401, "Unauthorized")
        ));
        response.setStatus(401);
    }
}