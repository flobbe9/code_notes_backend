package net.code_notes.backend;

import org.springframework.context.annotation.Import;

/**
 * Annotate webMvc tests with {@code @Import({SecurityTestConfig.class})} in order to test security
 * 
 * @since latest
 */
@Import({
    Oauth2TestConfig.class,
    // SecurityConfig.class // uncomment this when testint security
})
public class SecurityTestConfig {
    
}
