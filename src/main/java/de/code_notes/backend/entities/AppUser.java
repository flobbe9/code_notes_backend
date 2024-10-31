package de.code_notes.backend.entities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.code_notes.backend.abstracts.AbstractEntity;
import de.code_notes.backend.abstracts.AppUserRole;
import de.code_notes.backend.helpers.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity defining a user. Uses {@code email} as only equals and hashcode criteria. Implements {@link UserDetails}.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AppUser extends AbstractEntity implements UserDetails {

    @Column(nullable = false, unique = true)
    @Pattern(regexp = Utils.EMAIL_REGEX, message = "'email' does not match pattern")
    @Schema(example = "max.mustermann@domain.com")
    @EqualsAndHashCode.Include
    private String email;

    /** Unique, immutable id of an oauth2 user */
    @Column(unique = true, updatable = false)
    @Nullable
    private String oauth2Id;

    /** Null only for oauth2 users */
    @Nullable
    @Schema(example = "Abc123,.")
    // dont add a pattern here because it might not match the encrypted password, pattern is validated in "AppUserService.java"
    private String password;

    @Column(nullable = false)
    @NotNull(message = "'role' cannot be null")
    @Enumerated(EnumType.STRING)
    private AppUserRole role;

    /** Indicates whether the registration process has been completed or not. Default should be {@code false} */
    private boolean enabled;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @Nullable
    private List<@Valid @NotNull(message = "'appUser.tag' cannot be null") Tag> tags;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.REMOVE)
    @Nullable
    @JsonIgnore
    private List<@Valid @NotNull(message = "'appUser.note' cannot be null") Note> notes;


    public AppUser(String email, String password, AppUserRole role) {

        this.email = email;
        this.password = password;
        this.role = role;
    }


    /**
     * Will give appuser a default role of {@code USER} since there's no specific role concept for oauth2 providers.
     * 
     * @param oauth2User retrieved from session
     * @return an app user instance containing fields of given oauth user or {@code null} if {@code oauth2User} is {@code null}
     */
    public static AppUser getInstanceByDefaultOauth2User(@Nullable DefaultOAuth2User oauth2User) {

        if (oauth2User == null)
            return null;
        
        AppUser appUser = new AppUser();
        appUser.setEmail(oauth2User.getAttribute("email"));
        appUser.setRole(AppUserRole.USER);
        appUser.setOauth2Id(oauth2User.getAttribute("sub"));
        appUser.enable();

        return appUser;
    }


    /**
     * @param oauth2User retrieved from session
     * @param emailUserInfo fetched in addition to default user user info contained in {@code oauth2User}. Contains
     *                      the primary email for github user
     * @return an app user instance containing fields of given oauth user or {@code null} if (and only if) {@code oauth2User} is {@code null}
     */
    public static AppUser getInstanceByGithubUser(DefaultOAuth2User oauth2User, Map<String, Object> emailUserInfo) {

        if (oauth2User == null)
            return null;

        AppUser appUser = getInstanceByDefaultOauth2User(oauth2User);

        // case: not a github session
        if (emailUserInfo == null)
            return appUser;

        appUser.setEmail((String) emailUserInfo.get("email"));

        Integer oauth2Id = oauth2User.getAttribute("id");
        appUser.setOauth2Id(oauth2Id == null ? null : oauth2Id.toString());
        appUser.enable();

        return appUser;
    }


    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Set.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }


    @Override
    public String getPassword() {

        return this.password;
    }


    @Override
    @JsonIgnore
    public String getUsername() {

        return this.email;
    }


    @Override
    public boolean isEnabled() {

        return this.enabled;
    }
    

    @Override
    @JsonIgnore 
    public boolean isAccountNonExpired() {

        return true;
    }


    @Override
    @JsonIgnore 
    public boolean isAccountNonLocked() {

        return true;
    }


    @Override
    @JsonIgnore 
    public boolean isCredentialsNonExpired() {

        return true;
    }


    /**
     * Shorthand for {@code this.enabled = true}
     */
    public void enable() {

        this.enabled = true;
    }
}