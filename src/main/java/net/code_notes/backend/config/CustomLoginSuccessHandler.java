package net.code_notes.backend.config;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlank;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.code_notes.backend.helpers.CustomExceptionHandler;
import net.code_notes.backend.helpers.Utils;
import net.code_notes.backend.services.AppUserService;
import net.code_notes.backend.services.Oauth2Service;


/**
 * Class handling login success. Implements {@link AuthenticationSuccessHandler} and is beeing used in {@link SecurityConfig}
 * 
 * @since 0.0.1
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    /** The url query param key that is appended to the redirect url to start page. Also hard coded in "constatns.ts" */
    public static final String OAUTH2_LOGIN_ERROR_STATUS_URL_QUERY_PARAM = "oauth2-login-error";

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private Oauth2Service oauth2Service;

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;


    /**
     * Save oauth2 user. If is normal login write csrf token to resopnse output. If is oauth2 login
     * append csrf token as query param and redirect to frontend start page.<p>
     * 
     * Will logout if error.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        boolean isOauth2 = this.oauth2Service.isOauth2Session(authentication.getPrincipal());

        try {
            this.appUserService.saveCurrentOauth2(authentication.getPrincipal());

            CsrfToken csrfToken = getCsrfTokenByHttpRequest(request);
            String csrfTokenValue = csrfToken == null ? "" : csrfToken.getToken();
            
            // case: oauth2
            if (isOauth2)
                Utils.redirect(response, this.FRONTEND_BASE_URL + "/?%s=%s".formatted(Utils.CSRF_TOKEN_URL_QUERY_PARAM, csrfTokenValue));

            // case: normal login
            else
                Utils.writeToResponse(response, csrfTokenValue);

        } catch (Exception e) {
            CustomExceptionHandler.logPackageStackTrace(e);
            writeOrRedirectResponse(response, isOauth2, this.FRONTEND_BASE_URL + Utils.LOGIN_PATH, e);
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


    /**
     * Write response to outputstream or (if is oauth2) redirect to frontend appending the status code.
     * 
     * @param response
     * @param isOauth2
     * @param redirectPath
     * @param exception
     * @throws JsonProcessingException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private void writeOrRedirectResponse(HttpServletResponse response, boolean isOauth2, String redirectPath, Exception exception) throws JsonProcessingException, IllegalArgumentException, IOException {

        assertArgsNotNullAndNotBlank(response, 1, redirectPath, exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (exception instanceof ResponseStatusException)
            status = HttpStatus.valueOf(((ResponseStatusException) exception).getStatusCode().value());

        if (isOauth2)
            Utils.redirect(response, redirectPath + "/?%s=%s".formatted(OAUTH2_LOGIN_ERROR_STATUS_URL_QUERY_PARAM, status.value()));

        else
            Utils.writeToResponse(response, status, exception);
    }
}