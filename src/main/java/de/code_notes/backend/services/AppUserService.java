package de.code_notes.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.abstracts.AbstractService;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.helpers.Utils;
import de.code_notes.backend.repositories.AppUserRepository;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * @since 0.0.1
 */
@Service
@Log4j2
public class AppUserService extends AbstractService<AppUser> implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Save new or update given {@code appUser} depending on whether they have an {@code id} or not.
     * 
     * @param appUser to save
     * @return saved {@code appUser} or {@code null}
     * @throws IllegalArgumentException if {@code appUser} is {@code null}
     */
    public AppUser save(AppUser appUser) {

        // case: falsy param
        if (appUser == null)
            throw new IllegalArgumentException("Failed to save appUser. 'appUser' cannot be null");

        // case: appUser exists
        if (appUser.getId() != null)
            return update(appUser);

        return saveNew(appUser);
    }


    /**
     * Save given {@code appUser} assuming they have not been saved yet. This means the password should not be encrypted and
     * the email should not yet be taken.
     * 
     * @param appUser to save
     * @return the saved {@code appUser}
     * @throws IllegalArgumentException if given {@code appUser} is {@code null}
     * @throws ResponseStatusException if given {@code appUser} is invalid
     */
    private AppUser saveNew(AppUser appUser) throws IllegalArgumentException, ResponseStatusException {

        // case: falsy param
        if (appUser == null)
            throw new IllegalArgumentException("Failed to save new appUser. 'appUser' cannot be null");

        // validate
        validateAndThrowIncludePassword(appUser);

        // duplicate check
        if (exists(appUser))
            throw new ResponseStatusException(CONFLICT, "Failed to save new appUser. AppUser with this email does already exist");

        // encode password
        encodePassword(appUser);

        // save
        return this.appUserRepository.save(appUser);
    }


    /**
     * Update and save given {@code appUser} assuming that the password is encoded (wont be validated) and that the {@code appUser}'s id exists.
     * 
     * Changing the email is possible if the new one is unique.
     * 
     * @param appUser to update
     * @return updated {@code appUser}
     * @throws IllegalArgumentException if {@code appUser} is {@code null}
     * @throws ResponseStatusException if {@code appUser} is invalid
     */
    private AppUser update(AppUser appUser) {

        // case: falsy param
        if (appUser == null)
            throw new IllegalArgumentException("Failed to update appUser. 'appUser' cannot be null");

        AppUser oldAppUser = getById(appUser.getId());

        // case: no appUser with this id
        if (oldAppUser == null)
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Failed to update appUser. No appUser with given id.");

        // case: did change email 
        if (!oldAppUser.getEmail().equals(appUser.getEmail())) 
            // case: email already exists 
            if (exists(appUser))
                throw new ResponseStatusException(CONFLICT, "Failed to update appUser. AppUser with this email does already exist");

        // validate
        validateAndThrow(appUser);

        return this.appUserRepository.save(appUser);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return this.appUserRepository.findByEmail(username)
                                     .orElseThrow(() -> new UsernameNotFoundException("Failed to find user with by email."));
    }


    /**
     * Delete given {@code appUser} from db.
     * 
     * @param appUser 
     */
    public void delete(@Nullable Long id) {

        AppUser appUser = getById(id);

        // case: invalid id
        if (appUser == null)
            return;

        this.appUserRepository.delete(appUser);
    }


    /**
     * @param appUser
     * @return {@code true} if an app user with given {@code appUser.email} exists, {@code false} if not or is {@code null} (wont throw)
     */
    public boolean exists(@Nullable AppUser appUser) {

        if (appUser == null)
            return false;

        return this.appUserRepository.existsByEmail(appUser.getEmail());
    }


    /**
     * @param appUser to encode the password for
     * @throws IllegalArgumentException if {@code appUser} is {@code null}
     * @throws ResponseStatusException if {@code appUser.password} is blank
     */
    private void encodePassword(AppUser appUser) throws ResponseStatusException {

        // case: falsy param
        if (appUser == null)
            throw new IllegalArgumentException("Failed to encode password. 'appUser' cannot be null");
        
        // case: password blank
        if (Utils.isBlank(appUser.getPassword()))
            throw new ResponseStatusException(BAD_REQUEST, "Failed to encode password. 'appUser.password' cannot be blank");

        appUser.setPassword(this.passwordEncoder.encode(appUser.getPassword()));
    }


    /**
     * Call {@link #validateAndThrow} and validate the password in addition (since it's not annotated with any regex). 
     * 
     * @param appUser to validate
     * @return true if {@code appUser} is valid
     * @throws IllegalArgumentException if {@code appUser} is null
     * @throws ResponseStatusException if {@code appUser} is invalid
     */
    private boolean validateAndThrowIncludePassword(AppUser appUser) throws IllegalArgumentException, ResponseStatusException {

        if (appUser == null)
            throw new IllegalArgumentException("Failed to validate appUser. 'appUser' cannot be null");

        // validate all class annotations
        validateAndThrow(appUser);

        // validate password
        if (!appUser.getPassword().matches(Utils.PASSWORD_REGEX))
            throw new ResponseStatusException(BAD_REQUEST, "'appUser.password' does not match pattern");

        return true;
    }


    /**
     * @param id of {@code appUser}
     * @return {@code appUser} with given {@code id} of {@code null} (wont throw)
     */
    private AppUser getById(@Nullable Long id) {

        if (id == null)
            return null;

        return this.appUserRepository.findById(id).orElse(null);
    }
}