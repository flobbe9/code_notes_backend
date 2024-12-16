package de.code_notes.backend.config;

import static de.code_notes.backend.helpers.Utils.CONFIRM_ACCOUNT_PATH;
import static de.code_notes.backend.helpers.Utils.LOGIN_PATH;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;


/**
 * Configuration class to authentiacate requests.<p>
 * 
 * NOTE: this class autowires {@code AppUserService} via {@link CustomLoginSuccessHandler}.
 * 
 * @since 0.0.1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = false)
@Log4j2
public class SecurityConfig {

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;

    @Value("${FRONTEND_BASE_URL_WWW}")
    private String FRONTEND_BASE_URL_WWW;
    
    /**
     * Possible values:<p>
     * 
     * - {@code production}: login required, no develpment endpoints like swagger permitted, csrf enabled, .env.secrets not used<p>
     * - {@code qa}: login required, no develpment endpoints like swagger permitted, csrf enabled <p>
     * - {@code development}: no login required, all development endpoints like swagger permitted, csrf disabled
     */
    @Value("${ENV}")
    private String ENV;

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;
    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;
    @Autowired
    private CustomLoginFailureHandler customLoginFailureHandler;
    @Autowired
    private CustomUnAuthenticatedHandler customUnAuthenticatedHandler;


    @PostConstruct
    void init() {

        log.info("Configuring api security...");
    }

    
    /**
     * NOTE: RequestMatchers dont override each other. That's why order of calls matters.
     * 
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // case: development
        if (this.ENV.equalsIgnoreCase("development")) {
            http.csrf(csrf -> csrf.disable());

            http.authorizeHttpRequests(request -> request
                .anyRequest()
                    .permitAll());

        // case: qa | production
        } else {
            http.csrf(csrf -> csrf
                .ignoringRequestMatchers(getRoutesPriorToLogin())
                // load csrf token on every request
                .csrfTokenRequestHandler(customCsrfTokenRequestAttributeHandler()));

            // endpoints
            http.authorizeHttpRequests(request -> request
                .requestMatchers(getRoutesPriorToLogin())
                    .permitAll()
                .anyRequest()
                    .authenticated());
        }

        // login
        http.formLogin(formLogin -> formLogin
            .successHandler(this.customLoginSuccessHandler)
            .failureHandler(this.customLoginFailureHandler));

        http.oauth2Login(oauth2login -> oauth2login
            .successHandler(this.customLoginSuccessHandler)
            .failureHandler(this.customLoginFailureHandler));

        // logout
        http.logout(logout -> logout
            .logoutSuccessHandler(this.customLogoutSuccessHandler));

        // 401 (see "CustomExceptionHandler.java" for 403 handling)
        http.exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(this.customUnAuthenticatedHandler));

        // cors
        http.cors(cors -> cors
            .configurationSource(corsConfig()));

        return http.build();
    }


    /**
     * Allowing only certain urls to access this api. <p>
     * 
     * Used in filter chain: <p>
     * {@code http.cors(cors -> cors.configurationSource(corsConfig()))}
     * 
     * @return the configured {@link CorsConfigurationSource}
     */
    private CorsConfigurationSource corsConfig() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(this.FRONTEND_BASE_URL, this.FRONTEND_BASE_URL_WWW));
        configuration.setAllowedMethods(List.of("GET", "POST", "UPDATE", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    

    /**
     * Will make the csrf token available on login (and every other request).<p>
     * 
     * Used in security filter chain:<p>
     * {@code http.csrf(csrf -> csrf.csrfTokenRequestHandler(customCsrfTokenRequestAttributeHandler()));}
     * 
     * @return
     */
    private CsrfTokenRequestAttributeHandler customCsrfTokenRequestAttributeHandler() {

        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);

        return handler;
    }
    

    /**
     * @return array of paths that a user should be able to access without having a valid session, e.g. "/api/userService/register"
     */
    private String[] getRoutesPriorToLogin() {

        List<String> routesPriorLogin = new ArrayList<>(List.of(
            "/logout",
            LOGIN_PATH,
            "/app-user/register",
            CONFIRM_ACCOUNT_PATH,
            "/app-user/resend-confirmation-mail",
            "/app-user/check-logged-in",
            "/send-reset-password-mail",
            "/app-user/reset-password-by-token"
            ));

        // case: development
        if ("development".equalsIgnoreCase(this.ENV)) {
            routesPriorLogin.addAll(getSwaggerPaths()); 
            routesPriorLogin.addAll(List.of(
                "/app-user/save",
                "/test"
            ));
        }

        return routesPriorLogin.toArray(new String[routesPriorLogin.size()]);
    }


    /**
     * List of paths swagger uses. Assuming that no paths have been changed in properties file.
     * 
     * @return fixed size list of paths swagger uses
     */
    private List<String> getSwaggerPaths() {

        return List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/webjars/**"
        );
    }
}