package de.code_notes.backend.services;

import static de.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static de.code_notes.backend.helpers.Utils.assertPrincipalNotNullAndThrow401;
import static de.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
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
     * @param principal
     * @return the app user instance from db with same email as current user
     * @throws ResponseStatusException 401 if not logged in, 404 if the current app user does not exist in db
     */
    public AppUser loadCurrentFromDb(Object principal) throws ResponseStatusException {

        AppUser current = getCurrent(principal);

        return Optional.ofNullable(loadUser(current))
            .orElseThrow(
                () -> new ResponseStatusException(NOT_FOUND, "No user with this oauth2Id or email"));
    }


    /**
     * @return the app user instance from db with same email as current user
     * @throws ResponseStatusException 401 if not logged in, 404 if the current app user does not exist in db
     */
    public AppUser loadCurrentFromDb() throws ResponseStatusException {

        return loadCurrentFromDb(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
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
     * Retrieves app user with given {@code principal}'s oauth2Id and updates the app user with values of given {@code principal}.
     * If it's the first oauth2 login, either save {@code principal} as new appUser or, if the email address already existed, update that
     * existing one with given {@code principal}. Either way enable the appUser.<p>
     * 
     * Validates given {@code principal} before saving (assuming an oauth2 session).<p>
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

        AppUser oauth2AppUser = getCurrent(principal);

        AppUser existingOauth2AppUser = loadByOauth2Id(oauth2AppUser.getOauth2Id());

        // case: first oauth2 login
        if (existingOauth2AppUser == null) {
            // case: was already registered though, use that appUser
            if (existsByEmail(oauth2AppUser))
                existingOauth2AppUser = loadByEmail(oauth2AppUser.getEmail());

            else
                return save(oauth2AppUser);
        }

        existingOauth2AppUser.copyOauth2Fields(oauth2AppUser);
        existingOauth2AppUser.enable();

        return save(existingOauth2AppUser);
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

        assertArgsNotNullAndNotBlankOrThrow(appUser);

        if (!this.oauth2Service.isOauth2Session())
            validateAndThrowIncludePassword(appUser);
        else
            validateAndThrow(appUser);

        assertNotExistsOrThrow(appUser);

        if (!this.oauth2Service.isOauth2Session())
            enryptPassword(appUser);

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

        // validate
        validateAndThrow(appUser);

        AppUser oldAppUser = getById(appUser.getId());

        // case: no appUser with this id
        if (oldAppUser == null)
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Failed to update appUser. No appUser with given id");

        // case: did change email 
        if (!oldAppUser.getEmail().equals(appUser.getEmail())) 
            assertNotExistsOrThrow(appUser);

        // case: did change oauth2Id
        if (!isBlank(oldAppUser.getOauth2Id()) && !oldAppUser.getOauth2Id().equals(appUser.getOauth2Id()))
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Failed to update appUser. 'oauth2Id' must not be changed");

        return this.appUserRepository.save(appUser);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (isBlank(username))
            new UsernameNotFoundException("No app user with this blank username");

        return Optional.ofNullable(loadByOauth2Id(username))
            .or(() -> Optional.ofNullable(loadByEmail(username)))
            .orElseThrow(
                () -> new UsernameNotFoundException("No app user with this username"));
    }


    /**
     * Load given {@code appUser} from db.
     * 
     * @param appUser
     * @return appUser with given oauth2Id or (if blank) email or {@code null} if not found
     * @throws IllegalArgumentException
     */
    public AppUser loadUser(AppUser appUser) throws IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(appUser);

        try {
            return (AppUser) loadUserByUsername(isBlank(appUser.getOauth2Id()) ? appUser.getEmail() : appUser.getOauth2Id());

        } catch (UsernameNotFoundException e) {
            return null;
        }
    }
    

    /**
     * @param oauth2Id
     * @return the user with given {@code oauth2Id} or {@code null} (wont throw)
     */
    public AppUser loadByOauth2Id(@Nullable String oauth2Id) {

        if (isBlank(oauth2Id))
            return null;

        return this.appUserRepository.findByOauth2Id(oauth2Id).orElse(null);
    }
        

    /**
     * @param email
     * @return the user with given {@code email} or {@code null} (wont throw)
     */
    public AppUser loadByEmail(@Nullable String email) {

        if (isBlank(email))
            return null;

        return this.appUserRepository.findByEmail(email).orElse(null);
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

        AppUser appUser = loadCurrentFromDb();

        deleteRelatedEntities(appUser);

        this.appUserRepository.deleteById(appUser.getId());
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
     * @return {@code true} if an app user with given {@code appUser.id} exists, {@code false} if not or is {@code null} (wont throw)
     */
    public boolean exists(@Nullable AppUser appUser) {

        if (appUser == null || appUser.getId() == null)
            return false;

        return this.appUserRepository.existsById(appUser.getId());
    }


    /**
     * @param appUser
     * @return {@code true} if given {@code appUser} exists by email
     * @throws IllegalArgumentException
     */
    public boolean existsByEmail(AppUser appUser) throws IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(appUser, appUser.getEmail());

        return this.appUserRepository.existsByEmail(appUser.getEmail());
    }
    

    /**
     * @param appUser
     * @return {@code true} if an app user with given {@code appUser.oauth2Id} exists, {@code false} if not or is {@code null} (wont throw)
     */
    public boolean existsByOauth2Id(@Nullable AppUser appUser) {

        if (appUser == null || appUser.getOauth2Id() == null)
            return false;

        return this.appUserRepository.existsByOauth2Id(appUser.getOauth2Id());
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
     * Check that given {@code appUser} does not yet exist by any of their unique identifiers (email OR oauth2Id), throw otherwise. 
     * 
     * @param appUser 
     * @throws IllegalArgumentException
     * @throws ResponseStatusException 409 if exists
     */
    private void assertNotExistsOrThrow(AppUser appUser) throws IllegalArgumentException, ResponseStatusException {

        assertArgsNotNullAndNotBlankOrThrow(appUser);

        // case: registered normally
        if (appUser.getOauth2Id() == null && existsByEmail(appUser))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "'appUser' already exists by email");

        // case: registered with oauth2
        if (existsByOauth2Id(appUser) || existsByEmail(appUser))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "'appUser' already exists by email or oauth2Id");
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

        if (isBlank(appUser.getPassword()) || !appUser.getPassword().matches(Utils.PASSWORD_REGEX))
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
     * @throws IOException 
     * @throws IllegalStateException 
     */
    public AppUser register(String email, String password) throws ResponseStatusException, IllegalArgumentException, MessagingException, IllegalStateException, IOException {

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
     * @throws IOException 
     * @throws IllegalStateException 
     */
    public void resendAccountRegistrationConfirmationMail(String email) throws ResponseStatusException, IllegalArgumentException, MessagingException, IllegalStateException, IOException {

        assertArgsNotNullAndNotBlankOrThrow(email);

        AppUser appUser = loadByEmail(email);

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