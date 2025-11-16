package net.code_notes.backend.services;

import static net.code_notes.backend.helpers.Utils.CONFIRM_ACCOUNT_PATH;
import static net.code_notes.backend.helpers.Utils.LOGIN_PATH;
import static net.code_notes.backend.helpers.Utils.RESET_PASSWORD_PATH;
import static net.code_notes.backend.helpers.Utils.RESET_PASSWORD_TOKEN_URL_QUERY_PARAM;
import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static net.code_notes.backend.helpers.Utils.isBlank;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.ConfirmationToken;
import net.code_notes.backend.helpers.Utils;


/**
 * @since 0.0.1
 */
@Service
public class AsyncService {

    @Value("${BASE_URL}")
    private String BASE_URL;

    @Value("${GATEWAY_BASE_URL}")
    private String FRONTEND_BASE_URL;
    
    @Value("${GATEWAY_BASE_URL}" + Utils.PRIVACY_POLICY_PATH)
    private String DATA_POLICY_URL;
    
    @Value("${GATEWAY_BASE_URL}" + Utils.CONTACT_PATH)
    private String CONTACT_URL;

    @Value("classpath:mail/accountConfirmationMail.html")
    private Resource accountConfirmationMail;
    
    @Value("classpath:mail/resetPasswordMail.html")
    private Resource resetPasswordMail;
        
    @Value("classpath:mail/passwordHasBeenResetMail.html")
    private Resource passwordHasBeenResetMail;
            
    @Value("classpath:mail/appUserHasBeenDeletedMail.html")
    private Resource appUserHasBeenDeletedMail;
    
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

        String mailHtml = htmlMailToString(
            this.accountConfirmationMail, 
            this.FRONTEND_BASE_URL,
            this.BASE_URL + CONFIRM_ACCOUNT_PATH + "?token=" + confirmationToken.getToken(),
            this.DATA_POLICY_URL,
            this.CONTACT_URL
        );
            
        String subject = "Confirm your account | Code Notes";

        this.mailService.sendMail(
            confirmationToken.getAppUser().getEmail(), 
            subject, 
            mailHtml, 
            true, 
            Map.of("faviconWithLabel", Map.of(MailService.getFileAsAttachment(this.faviconWithLabel.getContentAsByteArray()), MediaType.IMAGE_PNG_VALUE)),
            null
        );
    }


    /**
     * Asyncronously sends a mail to the {@link AppUser} of given {@code confirmationToken} that contains the password reset link.<p>
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
    public void sendResetPasswordMail(ConfirmationToken confirmationToken) throws IllegalArgumentException, MessagingException, IllegalStateException, IOException {

        assertArgsNotNullAndNotBlankOrThrow(confirmationToken);

        String mailHtml = htmlMailToString(
            this.resetPasswordMail,
            this.FRONTEND_BASE_URL,
            this.FRONTEND_BASE_URL + RESET_PASSWORD_PATH + "?" + RESET_PASSWORD_TOKEN_URL_QUERY_PARAM + "=" + confirmationToken.getToken(),
            this.DATA_POLICY_URL,
            this.CONTACT_URL
        );
            
        String subject = "Reset password | Code Notes";

        this.mailService.sendMail(
            confirmationToken.getAppUser().getEmail(), 
            subject, 
            mailHtml, 
            true, 
            Map.of("faviconWithLabel", Map.of(MailService.getFileAsAttachment(this.faviconWithLabel.getContentAsByteArray()), MediaType.IMAGE_PNG_VALUE)),
            null
        );
    }


    @Async
    public void sendPasswordHasBeenResetMail(String to) throws IllegalArgumentException, IllegalStateException, IOException, MessagingException {

        assertArgsNotNullAndNotBlankOrThrow(to);

        String subject = "Password has been reset | Code Notes";

        String mailHtml = htmlMailToString(
            this.passwordHasBeenResetMail, 
            this.FRONTEND_BASE_URL,
            this.BASE_URL + "/app-user/send-reset-password-mail?to=" + to + "&redirectTo=" + this.FRONTEND_BASE_URL + LOGIN_PATH,
            this.DATA_POLICY_URL,
            this.CONTACT_URL 
        );

        this.mailService.sendMail(
            to, 
            subject, 
            mailHtml, 
            true, 
            Map.of("faviconWithLabel", Map.of(MailService.getFileAsAttachment(this.faviconWithLabel.getContentAsByteArray()), MediaType.IMAGE_PNG_VALUE)),
            null
        );
    }
    

    @Async
    public void sendAppUserHasBeenDeletedMail(String to) throws IllegalArgumentException, IllegalStateException, IOException, MessagingException {

        assertArgsNotNullAndNotBlankOrThrow(to);

        String subject = "Account deleted | Code Notes";

        String mailHtml = htmlMailToString(
            this.appUserHasBeenDeletedMail, 
            this.FRONTEND_BASE_URL,
            this.DATA_POLICY_URL,
            this.CONTACT_URL 
        );

        this.mailService.sendMail(
            to, 
            subject, 
            mailHtml, 
            true, 
            Map.of("faviconWithLabel", Map.of(MailService.getFileAsAttachment(this.faviconWithLabel.getContentAsByteArray()), MediaType.IMAGE_PNG_VALUE)),
            null
        );
    }


    /**
     * Parse the html mail file to string.
     * 
     * @param mailResource with the html mail
     * @param formatArgs args to pass to {@code String.format()} on the parsed mail string
     * @return the html mail content string
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private String htmlMailToString(Resource mailResource, @Nullable String ...formatArgs) throws IllegalStateException, IllegalArgumentException, IOException {

        assertArgsNotNullAndNotBlankOrThrow(mailResource);

        String mailString = Utils.fileToString(new ByteArrayInputStream(mailResource.getContentAsByteArray()));

        if (isBlank(mailString))
            throw new IllegalStateException("'mailResource' seems to be empty");

        return mailString.formatted((Object[]) formatArgs);
    }
}