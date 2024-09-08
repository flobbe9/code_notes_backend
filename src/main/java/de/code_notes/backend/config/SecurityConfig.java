package de.code_notes.backend.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;


/**
 * Configuration class to authentiacate requests.
 * 
 * @since 0.0.1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Log4j2
public class SecurityConfig {

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;

    @Value("${FRONTEND_BASE_URL_WWW}")
    private String FRONTEND_BASE_URL_WWW;
    
    /**
     * Possible values:<p>
     * 
     * - {@code prod}: login required, no develpment endpoints like swagger permitted, csrf enabled <p>
     * - {@code qa}: login required, some develpment endpoints like swagger permitted, csrf disabled <p>
     * - {@code dev}: no login required, all development endpoints like swagger permitted, csrf disabled
     */
    @Value("${ENV}")
    private String ENV;


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
        
        // case: dev
        if (this.ENV.equalsIgnoreCase("dev")) {
            http.csrf(csrf -> csrf.disable());

            http.authorizeHttpRequests(request -> request
                .anyRequest()
                    .permitAll());

        // case: qa or prod
        } else {
            // csrf
            http.csrf(csrf -> csrf
                .ignoringRequestMatchers(getRoutesPriorToLogin())
                // load csrf token on every request
                .csrfTokenRequestHandler(customCsrfTokenRequestAttributeHandler()));

            if (this.ENV.equalsIgnoreCase("qa"))
                http.csrf(csrf -> csrf.disable());

            // endpoints
            http.authorizeHttpRequests(request -> request
                .requestMatchers(getRoutesPriorToLogin())
                    .permitAll()
                .anyRequest()
                    .authenticated());

            // login
            http.formLogin(formLogin -> formLogin
                .successHandler(new CustomLoginSuccessHandler())
                .failureHandler(new CustomLogoutFailureHandler()));

            // logout
            http.logout(logout -> logout
                .logoutSuccessHandler(new CustomLogoutSuccessHandler()));

            // 401 (see "CustomExceptionHandler.java" for 403 handling)
            http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new CustomUnAuthenticatedHandler()));
        }

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
    // TODO: does this make a csrf token required on get requests?
        // add this to comment if so
    private CsrfTokenRequestAttributeHandler customCsrfTokenRequestAttributeHandler() {

        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);

        return handler;
    }

    
    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(10);
    }


    /**
     * @return array of paths that a user should be able to access without having a valid session, e.g. "/api/userService/register"
     */
    private String[] getRoutesPriorToLogin() {

        List<String> routesPriorLogin = new ArrayList<>(List.of(
            "/logout",
            "/login",
            "/register",
            "/confirmAccount",
            "/resendConfirmationMailByToken",
            "/resendConfirmationMailByEmail"
        ));

        // case: dev or qa 
        if (!"prod".equalsIgnoreCase(this.ENV)) {
            routesPriorLogin.addAll(getSwaggerPaths()); 
            routesPriorLogin.add("/appUser/save");
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