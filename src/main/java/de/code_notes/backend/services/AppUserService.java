package de.code_notes.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.helpers.Utils;
import de.code_notes.backend.repositories.AppUserRepository;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * @since 0.0.1
 */
@Service
// TODO: test all this
    // validation
    // methods
public class AppUserService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Validator validator;


    /**
     * Save new or update given {@code appUser} depending on whether they have an {@code id} or not.
     * 
     * @param appUser to save
     * @return saved {@code appUser} or {@code null}
     * @throws IllegalStateException if {@code appUser} is {@code null}
     */
    public AppUser save(AppUser appUser) {

        // case: falsy param
        if (appUser == null)
            throw new IllegalStateException("Failed to save appUser. 'appUser' cannot be null");

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
     * @throws IllegalStateException if given {@code appUser} is {@code null}
     * @throws ResponseStatusException if given {@code appUser} is invalid
     */
    private AppUser saveNew(AppUser appUser) throws IllegalStateException, ResponseStatusException {

        // case: falsy param
        if (appUser == null)
            throw new IllegalStateException("Failed to save new appUser. 'appUser' cannot be null");

        // validate
        validateAppUserAndThrow(appUser);

        // duplicate check
        if (exists(appUser))
            throw new ResponseStatusException(CONFLICT, "Failed to save new appUser. AppUser with this email does already exist");

        // encode password
        encodePassword(appUser);

        // save
        return this.appUserRepository.save(appUser);
    }


    // TODO: assume encoded password
    private AppUser update(AppUser appUser) {

        // null

        // exists

        // validate
        // TODO: expect password failure
        this.validator.validateObject(appUser);

        return this.appUserRepository.save(appUser);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return this.appUserRepository.findByEmail(username)
                                     .orElseThrow(() -> new UsernameNotFoundException("Failed to find user with by email."));
    }


    /**
     * @param appUser
     * @return {@code true} if given {@code appUser} exists, {@code false} if not or is {@code null} (wont throw)
     */
    private boolean exists(AppUser appUser) {

        if (appUser == null)
            return false;

            // TODO: does this find by email?
        return this.appUserRepository.exists(Example.of(appUser));
    }


    /**
     * @param appUser to encode the password for
     * @throws IllegalStateException if {@code appUser} is {@code null}
     * @throws ResponseStatusException if {@code appUser.password} is blank
     */
    private void encodePassword(AppUser appUser) throws ResponseStatusException {

        // case: falsy param
        if (appUser == null)
            throw new IllegalStateException("Failed to encode password. 'appUser' cannot be null");
        
        // case: password blank
        if (Utils.isBlank(appUser.getPassword()))
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Failed to encode password. 'appUser.password' cannot be blank");

        appUser.setPassword(this.passwordEncoder.encode(appUser.getPassword()));
    }


    /**
     * Validate given {@code appUser} using all annotations of it's entity. Throw if {@code appUser} is invalid.
     * 
     * @param appUser
     * @return true if {@code appUser} is valid
     * @throws IllegalStateException if {@code appUser} is null
     * @throws ResponseStatusException if {@code appUser} is invalid
     */
    private boolean validateAppUserAndThrow(AppUser appUser) throws IllegalStateException, ResponseStatusException {

        if (appUser == null)
            throw new IllegalStateException("Failed to validate appUser. 'appUser' cannot be null");

        this.validator.validateObject(appUser)
                      .failOnError((message) -> new ResponseStatusException(NOT_ACCEPTABLE, "Failed to save new appUser. 'appUser' is invalid"));

        return true;
    }
}