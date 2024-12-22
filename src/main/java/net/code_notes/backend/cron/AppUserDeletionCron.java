package net.code_notes.backend.cron;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.repositories.AppUserRepository;
import net.code_notes.backend.services.AppUserService;
import net.code_notes.backend.services.DeletedEntityRecordService;


/**
 * @since 0.0.1
 */
@Component
@Log4j2
public class AppUserDeletionCron {

    private static final int DAYS_BEFORE_DELETION = 14;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private DeletedEntityRecordService deletedEntityRecordService;

    
    @Scheduled(cron = "0 0 2 * * ?") // at 02:00 every day
    // @Scheduled(cron = "*/5 * * * * ?") // every 5 seconds
    void deleteDisabled() {

        log.info("Deleting disabled app users older than %d days...".formatted(DAYS_BEFORE_DELETION));

        List<AppUser> appUsersToDelete = this.appUserRepository.findByEnabledFalseAndCreatedBefore(LocalDateTime.now().minusDays(DAYS_BEFORE_DELETION));
        
        appUsersToDelete
            .forEach(appUser -> {
                this.deletedEntityRecordService.saveFor(appUser);
                this.appUserService.delete(appUser.getId());
            });

        log.info("Finished deleting disabled app users");
    }
}