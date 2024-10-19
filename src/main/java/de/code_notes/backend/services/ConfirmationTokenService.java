package de.code_notes.backend.services;

import static de.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static de.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import de.code_notes.backend.abstracts.AbstractService;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.ConfirmationToken;
import de.code_notes.backend.repositories.ConfirmationTokenRepository;
import jakarta.annotation.Nullable;


/**
 * @since 0.0.1
 */
@Service
public class ConfirmationTokenService extends AbstractService<ConfirmationToken> {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;


    @Override
    protected ConfirmationToken saveNew(ConfirmationToken confirmationToken) throws ResponseStatusException, IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(confirmationToken);

        validateAndThrow(confirmationToken);

        if (this.confirmationTokenRepository.existsByTokenAndAppUser(confirmationToken.getToken(), confirmationToken.getAppUser()))
            throw new ResponseStatusException(CONFLICT, "Confirmation token does already exist by 'token' and 'appUser'");

        confirmationToken = this.confirmationTokenRepository.save(confirmationToken);

        return confirmationToken;
    }


    @Override
    protected ConfirmationToken update(ConfirmationToken confirmationToken) throws ResponseStatusException, IllegalArgumentException {
        
        assertArgsNotNullAndNotBlankOrThrow(confirmationToken);

        validateAndThrow(confirmationToken);

        if (!this.confirmationTokenRepository.existsByTokenAndAppUser(confirmationToken.getToken(), confirmationToken.getAppUser()))
            throw new ResponseStatusException(CONFLICT, "Confirmation token does not exist by 'token' and 'appUser'");

        confirmationToken = this.confirmationTokenRepository.save(confirmationToken);

        return confirmationToken;
    }


    /**
     * Deletes old confirmation token of given {@code appUser} (if present) and saves a new one that is valid for the full {@code ConfirmationToken.MINUTES_BEFORE_EXPIRED}.
     * 
     * @param appUser
     * @return the saved confirmation token
     * @throws ResponseStatusException
     * @throws IllegalArgumentException
     */
    public ConfirmationToken createNew(AppUser appUser) throws ResponseStatusException, IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(appUser);

        deleteByAppUser(appUser);

        ConfirmationToken confirmationToken = new ConfirmationToken(appUser);

        return save(confirmationToken);
    }


    /**
     * Wont throw if no token with given {@code appUser}.
     * 
     * @param appUser
     * @throws IllegalStateException if {@code appUser} is null
     */
    private void deleteByAppUser(AppUser appUser) throws IllegalStateException {

        assertArgsNotNullAndNotBlankOrThrow(appUser);

        this.confirmationTokenRepository.deleteByAppUser(appUser);
    }


    /**
     * Make sure given token is valid and not confirmed already etc., then confirm it and save.
     * 
     * @param confirmationToken
     * @return
     * @throws ResponseStatusException in this order: 404 if no confirmation token with given token value, 202 if already confirmed, 406 if expired
     * @throws IllegalArgumentException
     */
    public ConfirmationToken confirm(ConfirmationToken confirmationToken) throws ResponseStatusException, IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(confirmationToken);

        if (confirmationToken == null)
            throw new ResponseStatusException(NOT_FOUND, "No confirmation token with given 'token' value");

        if (confirmationToken.isConfirmed())
            throw new ResponseStatusException(ACCEPTED, "Confirmation token already confirmed");

        if (confirmationToken.isExpired())
            throw new ResponseStatusException(NOT_ACCEPTABLE, "Confirmation token expired");

        confirmationToken.confirm();

        return save(confirmationToken);
    }


    public ConfirmationToken getByToken(@Nullable String token) {

        if (isBlank(token))
            return null;

        return this.confirmationTokenRepository.findByToken(token).orElse(null);
    }
}