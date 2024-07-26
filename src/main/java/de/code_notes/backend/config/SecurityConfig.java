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
@EnableMethodSecurity
@Log4j2
public class SecurityConfig {

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;
    
    /**
     * Possible values:<p>
     * 
     * - {@code prod}: login required, no develpment endpoints like swagger permitted, csrf enabled <p>
     * - {@code qa}: login required, some develpment endpoints like swagger permitted, csrf disabled <p>
     * - {@code dev}: no login required, all development endpoints like swagger permitted, scrf disabled
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
            http.csrf(csrf -> csrf
                // allow critical method types for paths prior to login
                .ignoringRequestMatchers(getRoutesPriorToLogin())
                // load csrf token on every request
                .csrfTokenRequestHandler(customCsrfTokenRequestAttributeHandler()));

            // case: qa
            if (this.ENV.equalsIgnoreCase("qa"))
                http.csrf(csrf -> csrf.disable());

            http.authorizeHttpRequests(request -> request
                // permitt all endpoints prior to login
                .requestMatchers(getRoutesPriorToLogin())
                    .permitAll()
                // restrict all other endpoints
                .requestMatchers("/**")
                    .authenticated());

            http.formLogin(formLogin -> formLogin
                .successHandler(new CustomAuthenticationSuccessHandler())
                .failureHandler(new CustomAuthenticationFailureHandler()));

            // logout successer url
            // login successer url
        }

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
        configuration.setAllowedOrigins(List.of(this.FRONTEND_BASE_URL));
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

        // case: dev or qa env
        if (!this.ENV.equalsIgnoreCase("prod"))
            routesPriorLogin.addAll(getSwaggerPaths()); 

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