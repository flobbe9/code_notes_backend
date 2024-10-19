package de.code_notes.backend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.helpers.Utils;
import de.code_notes.backend.services.AppUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Class handling login success. Implements {@link AuthenticationSuccessHandler} and is beeing used in {@link SecurityConfig}
 * 
 * @since 0.0.1
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AppUserService appUserService;
    

    /**
     * Save oauth2 user and write csrf token to resopnse output.<p>
     * 
     * Will logout if error.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        try {
            this.appUserService.saveCurrentOauth2(authentication.getPrincipal());

            CsrfToken csrfToken = getCsrfTokenByHttpRequest(request);
            
            // case: csrf not disabled
            if (csrfToken != null)
                Utils.writeToResponse(response, csrfToken.getToken());

        } catch (ResponseStatusException e) {
            Utils.writeToResponse(
                response, 
                HttpStatus.valueOf(e.getStatusCode().value()), 
                "Login failed. " + e.getMessage(), 
                true
            );
            this.appUserService.logout();
            
        } catch (Exception e) {
            Utils.writeToResponse(
                response, 
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Login failed. " + e.getMessage(), 
                true
            );
            this.appUserService.logout();
        }
    }


    /**
     * Attempts to load csrf token from given request. This will only work if the csrf token is loaded on every request. 
     * This can be ensured using the {@code CsrfTokenRequestAttributeHandler}. See {@code SecurityConfig}.
     * 
     * @param request to get the csrf token from
     * @return the csrf token object or {@code null} if not present
     */
    private CsrfToken getCsrfTokenByHttpRequest(HttpServletRequest request) {

        // case: falsy param
        if (request == null)
            return null;

        Object requestAttribute = request.getAttribute(CsrfToken.class.getName());

        return requestAttribute instanceof CsrfToken ? (CsrfToken) request.getAttribute(CsrfToken.class.getName()) : null;
    }
}