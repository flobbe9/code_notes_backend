package net.code_notes.backend.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.abstracts.AppUserRole;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.services.AppUserService;


/**
 * TODO
 * @since latest
 */
@Component
@Log4j2
public class CustomOauth2GrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {
    
    @Autowired
    private AppUserService appUserService;


    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {

        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        GrantedAuthority defaultRole = new SimpleGrantedAuthority(AppUserRole.USER.getSecurityName());

        for (GrantedAuthority authority : authorities) {
            if (authority instanceof OAuth2UserAuthority) {
                AppUser appUser = AppUser.getInstanceByDefaultOauth2User(((OAuth2UserAuthority) authority).getAttributes());
                appUser = this.appUserService.loadByEmail((String) appUser.getEmail());

                // case: was not registered yet
                if (appUser == null)
                    mappedAuthorities.add(defaultRole);

                else 
                    mappedAuthorities.addAll(appUser.getAuthorities());

            // case: unexpected authority type
            } else {
                log.debug("Failed to determine oauth2 authority during login. Passing default role 'USER'");
                mappedAuthorities.add(defaultRole);
            }

            // only iterate once
            break;
        }

        log.info(mappedAuthorities);

        return mappedAuthorities;
    }
}