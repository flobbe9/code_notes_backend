package de.code_notes.backend.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import de.code_notes.backend.controllers.CustomExceptionFormat;
import de.code_notes.backend.helpers.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Set the logout resopnse to a {@link CustomExceptionFormat} object with status 200.
 * 
 * @since 0.0.1
 */
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Utils.writeToResponse(response, HttpStatus.OK, "Logged out");
    }
}