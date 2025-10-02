package net.code_notes.backend.services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Getter;
import lombok.Setter;


/**
 * Proxy for {@code Oauth2Service} in order to use session scoped variables. 
 * 
 * @since 1.0.0
 */
@Service
@SessionScope
public class Oauth2ServiceProxy {
    
    /** The user info object containing the primary email of a github user. Will be set in {@link #getCurrentGithub()} */
    @Getter
    @Setter
    private Map<String, Object> currentPrimaryGithubEmailUserInfo;
}