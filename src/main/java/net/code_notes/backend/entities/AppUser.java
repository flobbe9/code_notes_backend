package net.code_notes.backend.entities;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.code_notes.backend.abstracts.AbstractEntity;
import net.code_notes.backend.abstracts.AppUserRole;
import net.code_notes.backend.abstracts.NeedsDeletionRecord;
import net.code_notes.backend.helpers.Utils;


/**
 * Entity defining a user. Uses {@code email} as only equals and hashcode criteria. Implements {@link UserDetails}.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AppUser extends AbstractEntity implements UserDetails, NeedsDeletionRecord {

    @Column(unique = true, nullable = false)
    @Pattern(regexp = Utils.EMAIL_REGEX, message = "'email' does not match pattern") // includes "notBlank"
    @Schema(example = "max.mustermann@domain.com")
    private String email;

    /** Unique, immutable id of an oauth2 user */
    @Column(unique = true)
    @Nullable
    private String oauth2Id;

    /** Null only for oauth2 users */
    @Nullable
    @Schema(example = "Abc123,.")
    // NOTE: dont add a pattern here because it might not match the encrypted password, pattern is validated in "AppUserService.java"
    private String password;

    @Column(nullable = false)
    @NotNull(message = "'role' cannot be null")
    @Enumerated(EnumType.STRING)
    private AppUserRole role;

    /** Indicates whether the registration process has been completed or not. Default should be {@code false} */
    @JsonIgnore
    private boolean enabled;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @Nullable
    // NOTE: don't make this an order column, since tags are saved on note-save
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


    /**
     * Update all fields of {@code this} instance that are retrieved from an oauth2 user.
     * 
     * @param oauth2AppUser containing values to use
     * @return {@code this}
     * @throws IllegalArgumentException
     */
    public AppUser copyOauth2Fields(AppUser oauth2AppUser) throws IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(oauth2AppUser);

        this.email = oauth2AppUser.getEmail();
        this.oauth2Id = oauth2AppUser.getOauth2Id();

        return this;
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

        if (this.oauth2Id != null)
            return this.oauth2Id;

        return this.email;
    }


    @Override
    @JsonIgnore
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


    @Override
    public String getUniqueId() {

        return this.email;
    }
}