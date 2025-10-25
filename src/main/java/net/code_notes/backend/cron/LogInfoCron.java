package net.code_notes.backend.cron;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.helpers.Utils;

@Component
@Log4j2
public class LogInfoCron {
        
    // @Scheduled(cron = "*/5 * * * * ?") // every 5 seconds
    // @Scheduled(cron = "0 */30 * * * ?") // every 30 minutes
    public void logInfo() {
        log.info("Logging infos:");

        Utils.logHeapSpaceInfo();

        log.info("Done logging infos");
    }
}
