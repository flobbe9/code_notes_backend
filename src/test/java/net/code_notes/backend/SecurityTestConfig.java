package net.code_notes.backend;

import org.springframework.context.annotation.Import;

import net.code_notes.backend.config.SecurityConfig;

/**
 * Annotate webMvc tests with {@code @Import({SecurityTestConfig.class})} in order to test security
 * 
 * @since 1.1.0
 */
@Import({
    Oauth2TestConfig.class,
    SecurityConfig.class // uncomment this when testint security
})
public class SecurityTestConfig {
    
}
