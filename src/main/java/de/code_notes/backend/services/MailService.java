package de.code_notes.backend.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


/**
 * @since 0.0.1
 */
@Service
public class MailService {

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${DEFAULT_SENDER_EMAIL}")
    private String DEFAULT_SENDER_EMAIL;

    @Value("${SMTP_AUTH_ENABLE}")
    private String SMTP_AUTH_ENABLE;

    @Value("${MAIL_STARTTLS_ENABLE}")
    private String MAIL_STARTTLS_ENABLE;

    @Value("${APP_ENV}")
    private String APP_ENV;


    @PostConstruct
    void init() {

        initJavaMailSender();
    }

    
    /**
     * Simple mail sending method. Uses 'spring.mail.username' property as sender mail.
     * 
     * @param to reciever email address
     * @param from sender email address
     * @param subject of email
     * @param text content of email
     * @param html true if {@code text} is written as HTML, else false
     * @param inlines map of <contentId, file>. {@code contentId} has to be referenced in the html document like this: 
     *               {@code <img src="cid:contentId" />}
     * @param attachments map of {@code <FileName, FileContent>} to attach to email (@see ByteArrayResource)
     * @throws MessagingException 
     */
    @Async
    public void sendMail(String to, 
                         String from,
                         String subject, 
                         String text, 
                         boolean html, 
                         Map<String, File> inlines,
                         @Nullable Map<String, InputStreamSource> attachments
    ) throws MessagingException {
        MimeMessage mimeMessage = createMimeMessage(to, from, subject, text, html, inlines, attachments);
        
        javaMailSender.send(mimeMessage);
    }
        

    /**
     * Overload. Use {@link #MAIL_SENDER_EMAIL} as sender email address.
     * 
     * @param to reciever email address
     * @param subject of email
     * @param text content of email
     * @param html true if {@code text} is written as HTML, else false
     * @param inlines map of <contentId, file>. {@code contentId} has to be referenced in the html document like this: 
     *               {@code <img src="cid:contentId" />}
     * @param attachments map of {@code <FileName, FileContent>} to attach to email (@see ByteArrayResource)
     * @throws MessagingException 
     */
    public void sendMail(String to, 
                         String subject, 
                         String text, 
                         boolean html, 
                         Map<String, File> inlines,
                         @Nullable Map<String, InputStreamSource> attachments
    ) throws MessagingException {

        sendMail(to, this.DEFAULT_SENDER_EMAIL, subject, text, html, inlines, attachments);
    }


    /**
     * Overload.
     * 
     * @param to reciever email address
     * @param subject of email
     * @param text content of email
     * @param html true if {@code text} is written as HTML, else false
     * @param inlines map of <contentId, file>. {@code contentId} has to be referenced in the html document like this: 
     *               {@code <img src="cid:contentId" />}
     * @param attachments files to attach to email, optional
     * @throws MessagingException 
     */
    public void sendMail(String to, 
                         String subject, 
                         String text, 
                         boolean html, 
                         Map<String, File> inlines,
                         @Nullable List<File> attachments
    ) throws MessagingException {

        sendMail(to, subject, text, html, inlines, getFilesAsAttachments(attachments));
    }
    

    /**
     * Create simple {@code mimeMessage} for mail sending method.
     * 
     * @param to reciever email address
     * @param from sender email address
     * @param subject of email
     * @param text content of email
     * @param html true if 'text' is written as HTML, else false
     * @param attachments map of {@code <FileName, FileContent>} to attach to email (@see ByteArrayResource)
     * @param inlines map of <contentId, file>. {@code contentId} has to be referenced in the html document like this: 
     *               {@code <img src="cid:contentId" />}
     * @return simple mimeMessage with given attributes
     * @throws MessagingException
     */
    private MimeMessage createMimeMessage(
        String to, 
        String from,
        String subject, 
        String text, 
        boolean html, 
        Map<String, File> inlines,
        Map<String, InputStreamSource> attachments
    ) throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED);

        helper.addTo(to);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(text, html);

        if (inlines != null)
            addInlines(helper, inlines);

        if (attachments != null)
            addAttatchments(helper, attachments);
        
        return mimeMessage;
    }
    
    
    private void addAttatchments(@Nullable MimeMessageHelper helper, @Nullable Map<String, InputStreamSource> attachments) throws MessagingException {

        if (helper == null || attachments == null)
            return;

        for (Entry<String, InputStreamSource> entry : attachments.entrySet()) {
            // case: file content is null
            if (entry.getValue() == null)
                continue;

            helper.addAttachment(entry.getKey(), entry.getValue());
        }
    }


    private void addInlines(@Nullable MimeMessageHelper helper, @Nullable Map<String, File> inlines) throws MessagingException {

        if (helper == null || inlines == null)
            return;

        for (Entry<String, File> entry : inlines.entrySet()) {
            String contentId = entry.getKey();
            File file = entry.getValue();

            helper.addInline(contentId, file);
        };
    }

    
    /**
     * @param files to convert
     * @return map of {@code <FileName, FileContent>}. The ByteArrayResource may be {@code null} in case of an exception
     */
    private Map<String, InputStreamSource> getFilesAsAttachments(@Nullable List<File> files) {

        if (files == null)
            return null;

        return files
            .stream()
            .collect(Collectors.toMap(file -> 
                // file name
                file.getName(), 

                // file bytes
                file -> {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        return new ByteArrayResource(fis.readAllBytes());
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            ));
    }


    /**
     * Set some properties for the {@link #javaMailSender}.
     */
    private void initJavaMailSender() {
        
        Properties props = this.javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", this.SMTP_AUTH_ENABLE);
        props.put("mail.smtp.starttls.enable", this.MAIL_STARTTLS_ENABLE);

        // debug mode
        // if (APP_ENV.equals("dev"))
        //     props.put("mail.debug", "true");
    }
}