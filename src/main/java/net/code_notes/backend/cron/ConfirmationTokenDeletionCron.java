package net.code_notes.backend.cron;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.services.ConfirmationTokenService;


/**
 * @since 0.0.1
 */
@Component
@Log4j2
public class ConfirmationTokenDeletionCron {

    private static final int MONTHS_BEFORE_DELETION = 6;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;


    @Scheduled(cron = "0 0 2 * * ?") // at 02:00 every day
    // @Scheduled(cron = "*/5 * * * * ?") // every 5 seconds
    public void deleteOlderThan() {

        log.info("Deleting old confirmation tokens...");

        this.confirmationTokenService.deleteOldConfirmationTokens(MONTHS_BEFORE_DELETION);

        log.info("Finished deleting old confirmation tokens.");
    }
}