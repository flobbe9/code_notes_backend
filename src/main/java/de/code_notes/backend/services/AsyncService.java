package de.code_notes.backend.services;

import static de.code_notes.backend.helpers.Utils.CONFIRM_ACCOUNT_PATH;
import static de.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static de.code_notes.backend.helpers.Utils.isBlank;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.ConfirmationToken;
import de.code_notes.backend.helpers.Utils;
import jakarta.mail.MessagingException;


/**
 * @since 0.0.1
 */
@Service
public class AsyncService {

    @Value("${BASE_URL}")
    private String BASE_URL;

    @Value("${FRONTEND_BASE_URL}")
    private String FRONTEND_BASE_URL;
    
    @Value("${FRONTEND_BASE_URL}/data-policy")
    private String DATA_POLICY_URL;
    
    @Value("${FRONTEND_BASE_URL}/about")
    private String ABOUT_URL;

    @Value("classpath:mail/accountConfirmationMail.html")
    private Resource accountConfirmationMail;

    @Value("classpath:static/favicon.ico")
    private Resource favicon;
    
    @Value("classpath:assets/img/faviconWithLabel.png")
    private Resource faviconWithLabel;

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
     * @throws IOException 
     * @throws IllegalStateException 
     */
    @Async
    public void sendAccountRegistrationConfirmationMail(ConfirmationToken confirmationToken) throws IllegalArgumentException, MessagingException, IllegalStateException, IOException {

        assertArgsNotNullAndNotBlankOrThrow(confirmationToken);

        String mailHtml = getAccountRegistrationConfirmationMail(this.BASE_URL + CONFIRM_ACCOUNT_PATH + "?token=" + confirmationToken.getToken());
            
        String subject = "Confirm your Account | Code Notes";

        this.mailService.sendMail(
            confirmationToken.getAppUser().getEmail(), 
            subject, 
            mailHtml, 
            true, 
            Map.of("faviconWithLabel", Map.of(MailService.getFileAsAttachment(this.faviconWithLabel.getContentAsByteArray()), MediaType.IMAGE_PNG_VALUE)),
            Map.of(this.favicon.getFilename(), MailService.getFileAsAttachment(this.favicon.getContentAsByteArray()))
        );
    }


    /**
     * Parse the html mail file to string and add the necessary variables to it.
     * 
     * @param confirmationLink endpoint to confirm the account. See {@code AppUserController}
     * @return the html mail content
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private String getAccountRegistrationConfirmationMail(String confirmationLink) throws IllegalStateException, IllegalArgumentException, IOException {

        if (this.accountConfirmationMail == null)
            throw new IllegalStateException("Failed to find 'accountConfirmationMail.html'");

        if (isBlank(confirmationLink))
            throw new IllegalStateException("'confirmationLink' cannot be blank");

        String accountConfirmationMailString = Utils.fileToString(new ByteArrayInputStream(this.accountConfirmationMail.getContentAsByteArray()));

        if (isBlank(accountConfirmationMailString))
            throw new IllegalStateException("'accountConfirmationMail.html' seems to be empty");

        return accountConfirmationMailString.formatted(
            this.FRONTEND_BASE_URL,
            confirmationLink,
            this.DATA_POLICY_URL,
            this.ABOUT_URL
        );
    }
}