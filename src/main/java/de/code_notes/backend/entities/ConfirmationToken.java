package de.code_notes.backend.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.code_notes.backend.abstracts.AbstractEntity;
import de.code_notes.backend.helpers.Utils;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;


/**
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ConfirmationToken extends AbstractEntity {

    /** Also hard coded in "constants.ts" */
    private static final int HOURS_BEFORE_EXPIRED = 12;
    
    @Column(nullable = false)
    @NotBlank(message = "'token' cannot be blank")
    @EqualsAndHashCode.Include
    private String token;

    @OneToOne
    @JoinColumn(name = "app_user_id", nullable = false)
    @NotNull(message = "'appUser' cannot be null")
    @EqualsAndHashCode.Include
    private AppUser appUser;

    @Nullable
    @JsonFormat(pattern = Utils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    @NotNull(message = "'expiresAt' cannot be null")
    @JsonFormat(pattern = Utils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime expiresAt;


    /**
     * Sets expiration time to {@link #HOURS_BEFORE_EXPIRED} minutes.
     */
    public ConfirmationToken(AppUser appUser) {

        this.token = UUID.randomUUID().toString();
        this.appUser = appUser;
        this.expiresAt = LocalDateTime.now().plusHours(HOURS_BEFORE_EXPIRED);
    }


    public ConfirmationToken(AppUser appUser, int hoursUntilExpired) {
        
        this.token = UUID.randomUUID().toString();
        this.appUser = appUser;
        this.expiresAt = LocalDateTime.now().plusHours(hoursUntilExpired);
    }


    public boolean isExpired() {

        return this.expiresAt.isBefore(LocalDateTime.now());
    }


    public boolean isConfirmed() {

        return this.confirmedAt != null;
    }


    /**
     * Equivalent of {@code setConfirmedAt(LocalDateTime.now())}
     */
    public void confirm() {

        this.confirmedAt = LocalDateTime.now();
    }
}