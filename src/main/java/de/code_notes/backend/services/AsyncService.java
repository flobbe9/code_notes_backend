package de.code_notes.backend.services;

import static de.code_notes.backend.helpers.Utils.CONFIRM_ACCOUNT_PATH;
import static de.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.ConfirmationToken;
import jakarta.mail.MessagingException;


/**
 * @since 0.0.1
 */
@Service
public class AsyncService {

    @Value("${BASE_URL}")
    private String BASE_URL;

    @Autowired
    private MailService mailService;
    

    /**
     * Asyncronously sends a mail to the {@link AppUser} of given {@code confirmationToken} that contains the confirmation link.<p>
     * 
     * Does not check or validate any confirmation value like the expired date etc.
     * 
     * @param confirmationToken that has the token value and the {@code appUser}
     * @throws IllegalArgumentException
     * @throws MessagingException if mail sending has failed (more likely to just log but not thorw, since mail sending happens asynchronously)
     */
    @Async
    public void sendAccountRegistrationConfirmationMail(ConfirmationToken confirmationToken) throws IllegalArgumentException, MessagingException {

        assertArgsNotNullAndNotBlankOrThrow(confirmationToken);

        String mailHtml = String.format(
            """
                Thank you for signing up for Code Notes!
                <br><br><br>

                Please confirm your account by clicking the link below:
                <br>
                <a href='%s' target='_blank'>Confirm Account</a>
                <br><br>

                Your haven't registered for Code Notes? Then you can safely ignore this message.
                <br><br><br>

                Kind regards,
                <br><br>
                
                The Code Notes Team
            """,
            String.format(this.BASE_URL + CONFIRM_ACCOUNT_PATH + "?token=" + confirmationToken.getToken())
        );
            
        String subject = "Confirm your Account | Code Notes";

        this.mailService.sendMail(confirmationToken.getAppUser().getEmail(), subject, mailHtml, true, null, null);
    }
}
