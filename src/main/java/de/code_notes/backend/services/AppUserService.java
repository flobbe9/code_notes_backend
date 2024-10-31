package de.code_notes.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.abstracts.AbstractService;
import de.code_notes.backend.abstracts.AppUserRole;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.ConfirmationToken;
import de.code_notes.backend.helpers.Utils;
import de.code_notes.backend.repositories.AppUserRepository;
import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.Map;

import static de.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static de.code_notes.backend.helpers.Utils.assertPrincipalNotNullAndThrow401;
import static de.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;


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
        
    @Autowired
    private Oauth2Service oauth2Service;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Autowired
    private AsyncService asyncService;

    /** The user info object containing the primary email of a github user. Will be set in {@link #getCurrentGithub()} */
    private Map<String, Object> currentPrimaryGithubEmailUserInfo;


    /**
     * @param principal the current app user
     * @return the app user currently logged in (not retrieving them from db) or throws (thus never {@code null})
     * @throws ResponseStatusException 401 if not logged in, 501 if the logged in principal is not of a handled type
     * @throws IllegalStateException 
     */
    public AppUser getCurrent(Object principal) throws ResponseStatusException, IllegalStateException {

        assertPrincipalNotNullAndThrow401(principal);

        // case: logged in with formLogin
        if (principal instanceof AppUser) 
            return (AppUser) principal;

        // case: logged in with oauth
        if (principal instanceof DefaultOAuth2User) {
            if (this.oauth2Service.isPrincipalGithubUser(principal))
                return getCurrentGithub(principal);

            return AppUser.getInstanceByDefaultOauth2User((DefaultOAuth2User) principal);
        }

        // case: login type not handled (should not happen)
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Failed to get current user. Instance of 'principal' not handled.");
    }


    /**
     * Overload. Use principal from {@code SecurityContextHolder.getContext().getAuthentication().getPrincipal()}
     * 
     * @return the app user currently logged in (not retrieving them from db) or throws (thus never {@code null})
     * @throws ResponseStatusException
     * @throws IllegalStateException
     */
    public AppUser getCurrent() throws ResponseStatusException, IllegalStateException {

        return getCurrent(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    /**
     * @return the app user instance from db with same email as current user
     * @throws ResponseStatusException 401 if not logged in, 404 if the current app user does not exist in db
     */
    public AppUser getCurrentFromDb() throws ResponseStatusException {

        AppUser current = getCurrent();

        return (AppUser) Optional.of(getByEmail(current.getEmail()))
            .orElseThrow(() -> 
                new ResponseStatusException(NOT_FOUND, "No user with this email"));
    }


    /**
     * Fetches the primary email address of the current github user since that is not sent along with the default user-info endpoint. Caches the retrieved user info
     * in order to minimize fetch calls.
     * 
     * @param principal that is logged in currently
     * @return the current app user instance (never {@code null})
     * @throws ResponseStatusException 401 if not logged in, 409 if is not an oauth2 session
     */
    private AppUser getCurrentGithub(Object principal) throws ResponseStatusException {

        assertPrincipalNotNullAndThrow401(principal);

        // case: not logged in with github
        if (!this.oauth2Service.isPrincipalGithubUser(principal))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to get current github user. Not logged in with github");

        DefaultOAuth2User oauthUser = ((DefaultOAuth2User) principal);
                
        // case: github email has been cached
        if (this.currentPrimaryGithubEmailUserInfo != null) 
            return AppUser.getInstanceByGithubUser(oauthUser, this.currentPrimaryGithubEmailUserInfo);
        
        Map<String, Object> primaryGithubEmailUserInfo = this.oauth2Service.fetchPrimaryGithubEmailUserInfo();

        // cache email user info
        this.currentPrimaryGithubEmailUserInfo = primaryGithubEmailUserInfo;

        return AppUser.getInstanceByGithubUser(oauthUser, primaryGithubEmailUserInfo);
    }


    /**
     * Retrieves app user with by given {@code principal}'s oauth2Id and updates the app user with values of given {@code principal}.<p>
     * 
     * Validates given {@code principal} before saving (assuming an oauth2 session).<p>
     * 
     * Wont save given {@code principal} if nothing has changed.<p>
     * 
     * Wont do anything if not an oauth2 session or (wont throw either).<p>
     * 
     * User will be assigned the role {@code USER}
     * 
     * @param principal that is currently logged in
     * @return the saved app user, the retrieved app user (not beeing saved) or {@code null} if not an oauth2 session
     * @throws ResponseStatusException 401, 406 if given {@code principal} is invalid
     */
    public AppUser saveCurrentOauth2(Object principal) throws ResponseStatusException {

        assertPrincipalNotNullAndThrow401(principal);

        if (!this.oauth2Service.isOauth2Session(principal))
            return null;

        AppUser sessionAppUser = getCurrent(principal);
        
        validateCurrentOauth2AndThrow(sessionAppUser);

        AppUser savedAppUser = getByOauth2Id(sessionAppUser.getOauth2Id());

        // case: new user
        if (savedAppUser == null)
            return this.appUserRepository.save(sessionAppUser);

        // case: app user already saved and has not been updated
        if (sessionAppUser.getEmail().equals(savedAppUser.getEmail()))
            return savedAppUser;

        savedAppUser.setEmail(sessionAppUser.getEmail());

        return this.appUserRepository.save(savedAppUser);
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
    @Override
    protected AppUser saveNew(AppUser appUser) throws IllegalArgumentException, ResponseStatusException {

        // case: falsy param
        if (appUser == null)
            throw new IllegalArgumentException("Failed to save new appUser. 'appUser' cannot be null");

        // validate
        validateAndThrowIncludePassword(appUser);

        // duplicate check
        if (exists(appUser))
            throw new ResponseStatusException(CONFLICT, "Failed to save new appUser. AppUser with this email does already exist");

        // encrypt password
        enryptPassword(appUser);

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
    @Override
    protected AppUser update(AppUser appUser) {

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


    /**
     * Wont throw.
     * 
     * @return {@link AppUser} with given username from db or {@code null}
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {

        if (isBlank(username))
            return null;

        return this.appUserRepository
            .findByEmail(username)
            .orElseThrow(
                () ->new UsernameNotFoundException("No app user with this username"));
    }


    /**
     * Basically overloading {@link #loadUserByUsername(String)}. Wont throw either
     * 
     * @param email
     * @return
     */
    public AppUser getByEmail(@Nullable String email) {

        try {
            UserDetails appUser = loadUserByUsername(email);

            return (AppUser) appUser;
        } catch (UsernameNotFoundException e) {
            return null;
        }
    }
    

    /**
     * @param oauth2Id
     * @return the user with given {@code oauth2Id} or {@code null} (wont throw)
     */
    public AppUser getByOauth2Id(@Nullable String oauth2Id) {

        if (isBlank(oauth2Id))
            return null;

        return this.appUserRepository.findByOauth2Id(oauth2Id).orElse(null);
    }


    /**
     * Delete appUser with given {@code id} from db. Wont throw if does not exist
     * 
     * @param id 
     */
    public void delete(@Nullable Long id) {

        if (id == null)
            return;

        deleteRelatedEntities(getById(id));

        this.appUserRepository.deleteById(id);
    }

    
    /**
     * @throws ResponseStatusException 401
     */
    public void deleteCurrent() {

        AppUser appUser = getCurrent();

        deleteRelatedEntities(appUser);

        this.appUserRepository.deleteByEmail(appUser.getEmail());
    }


    /**
     * Delete entities that reference given {@code appUser}.
     * 
     * @param appUser
     */
    private void deleteRelatedEntities(@Nullable AppUser appUser) {

        if (appUser == null)
            return;

        this.confirmationTokenService.deleteByAppUser(appUser);
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
    private void enryptPassword(AppUser appUser) throws ResponseStatusException {

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
     * Make sure {@code email} and {@code oauth2Id} are not blank.<p>
     * 
     * Wont throw if not an oauth2 session.
     * 
     * @param principal current oauth2 user
     * @throws ResponseStatusException 401, 406
     * @throws IllegalArgumentException if {@code principal} is {@code null}
     */
    private void validateCurrentOauth2AndThrow(Object principal) throws ResponseStatusException, IllegalArgumentException {

        assertPrincipalNotNullAndThrow401(principal);

        if (!this.oauth2Service.isOauth2Session(principal))
            return;

        AppUser appUser = getCurrent(principal);

        if (isBlank(appUser.getEmail()))
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Current oauth2 user is missing an email address");
            
        if (isBlank(appUser.getOauth2Id()))
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Current oauth2 user is missing an id");
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


    /**
     * Invalidates current session. Wont throw if already logged out.
     */
    public void logout() {

        SecurityContextHolder.clearContext();
        
        HttpSession session = Utils.getCurrentRequest().getSession(false);
        if (session != null) 
            session.invalidate();
    }


    /**
     * Save new {@link AppUser} with given credentials and a default role of {@code USER}, then send a confirmation mail.
     * 
     * @param email
     * @param password raw user input
     * @return the saved app user
     * @throws ResponseStatusException 406 if given email or password are invalid, 409 if email already taken, 
     * @throws IllegalArgumentException
     * @throws MessagingException if mail sending has failed (more likely to just log but not thorw, since mail sending happens asynchronously)
     */
    public AppUser register(String email, String password) throws ResponseStatusException, IllegalArgumentException, MessagingException {

        assertArgsNotNullAndNotBlankOrThrow(email, password);

        AppUser appUser = save(new AppUser(email, password, AppUserRole.USER));

        ConfirmationToken confirmationToken = this.confirmationTokenService.createNew(appUser);

        this.asyncService.sendAccountRegistrationConfirmationMail(confirmationToken);

        return appUser;
    }


    /**
     * Resends the account confirmation mail to given {@code email}. Needs an appUser to exist with given {@code email}
     * 
     * @param email 
     * @throws ResponseStatusException 202 app user already enabled, 404 if no app user exists with given {@code email}
     * @throws IllegalArgumentException
     * @throws MessagingException if mail sending has failed (more likely to just log but not thorw, since mail sending happens asynchronously)
     */
    public void resendAccountRegistrationConfirmationMail(String email) throws ResponseStatusException, IllegalArgumentException, MessagingException {

        assertArgsNotNullAndNotBlankOrThrow(email);

        AppUser appUser = getByEmail(email);

        if (appUser == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No app user with given 'email'");

        if (appUser.isEnabled())
            throw new ResponseStatusException(ACCEPTED, "Account is already confirmed");

        ConfirmationToken confirmationToken = this.confirmationTokenService.createNew(appUser);

        this.asyncService.sendAccountRegistrationConfirmationMail(confirmationToken);
    }


    /**
     * Confirms the {@link ConfirmationToken} and enables the {@link AppUser} linked to given {@code token} and saves both.
     * 
     * @param token value of a confirmation token
     * @return the saved and enabled app user
     * @throws ResponseStatusException 404 if no confirmation token for given token exists, 406 if the confirmation token is expired,
     *                                 202 if app user or confirmation token are already enabled / confirmed 
     * @throws IllegalArgumentException
     */
    public AppUser confirmAppUser(String token) throws ResponseStatusException, IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(token);

        ConfirmationToken confirmationToken = this.confirmationTokenService.getByToken(token);

        if (confirmationToken == null)
            throw new ResponseStatusException(NOT_FOUND, "No confirmation token found with given 'token' value");

        AppUser appUser = confirmationToken.getAppUser();

        if (appUser.isEnabled())
            throw new ResponseStatusException(ACCEPTED, "App user is already confirmed");

        this.confirmationTokenService.confirm(confirmationToken);

        appUser.enable();

        save(appUser);

        return appUser;
    }
}