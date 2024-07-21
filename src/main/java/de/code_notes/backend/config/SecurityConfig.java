package de.code_notes.backend.config;

import static de.code_notes.backend.helpers.Utils.prependSlash;

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
// TODO
public class SecurityConfig {

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;
    
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
        
        // allow anything
        if (!this.ENV.equalsIgnoreCase("prod")) {
            http.csrf(csrf -> csrf.disable());

            http.authorizeHttpRequests(request -> request
                .anyRequest()
                    .permitAll());

        // enable csrf and restrict url access
        } else {
            http.csrf(csrf -> csrf
                // allow critical method types for paths prior to login
                .ignoringRequestMatchers(getRoutesPriorToLogin()));   

            http.authorizeHttpRequests(request -> request
                // permitt some MAPPING endpoints
                .requestMatchers(getRoutesPriorToLogin())
                    .permitAll()
                // restrict all other MAPPING endpoints
                .requestMatchers("/**")
                    .authenticated()
                // allow all non MAPPING endpoints
                .anyRequest()
                    .permitAll());
        }

        // allow frontend
        http.cors(cors -> cors
            .configurationSource(corsConfig()));

        return http.build();
    }


    /**
     * Configure cors.
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
        source.registerCorsConfiguration("/" + "/**", configuration);

        return source;
    }

    
    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(10);
    }


    /**
     * @return array of paths that a user should be able to access without having a valid session, e.g. "/api/userService/register"
     */
    private String[] getRoutesPriorToLogin() {

        return new String[] {
            "/register",    
            "/confirmAccount",
            "/resendConfirmationMailByToken",
            "/resendConfirmationMailByEmail"
        };
    }
}