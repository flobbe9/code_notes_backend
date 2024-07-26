package de.code_notes.backend.config;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.helpers.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Class handling login success. Implements {@link AuthenticationSuccessHandler} and is beeing used in {@link SecurityConfig}
 * 
 * @since 0.0.1
 */
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Authentication authentication) throws IOException, ServletException {

        CsrfToken csrfToken = getCsrfTokenByHttpRequest(request);

        AppUser appUser = (AppUser) authentication.getPrincipal();

        // case: csrf enabled
        if (csrfToken != null)
            appUser.setCsrfToken(csrfToken.getToken());
            
        response.getWriter().write(Utils.getDefaultObjectMapper().writeValueAsString(appUser));
    }


    /**
     * Attempts to load csrf token from given request. This will only work if the csrf token is loaded on every request. 
     * This can be ensured using the {@code CsrfTokenRequestAttributeHandler}. * See {@code SecurityConfig}.
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