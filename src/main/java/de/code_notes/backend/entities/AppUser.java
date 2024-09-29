package de.code_notes.backend.entities;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
import jakarta.validation.constraints.NotBlank;
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

    @Column(nullable = false)
    @NotBlank(message = "'password' cannot be blank")
    @Schema(example = "Abc123,.")
    private String password;

    @Column(nullable = false)
    @NotNull(message = "'role' cannot be null")
    @Enumerated(EnumType.STRING)
    private AppUserRole role;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @Nullable
    private Set<@Valid Tag> tags;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.REMOVE)
    @Nullable
    @JsonIgnore
    private List<@Valid Note> notes;


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
    @JsonIgnore 
    public boolean isEnabled() {

        return true;
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
}