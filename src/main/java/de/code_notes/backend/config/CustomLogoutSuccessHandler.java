package de.code_notes.backend.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

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
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        response.getWriter().write(Utils.getDefaultObjectMapper().writeValueAsString(
            new CustomExceptionFormat(200, "Logged out")
        ));
    }
}