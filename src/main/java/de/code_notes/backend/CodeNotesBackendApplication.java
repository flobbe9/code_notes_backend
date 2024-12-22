package de.code_notes.backend;

import static de.code_notes.backend.helpers.Utils.isBlank;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.code_notes.backend.helpers.Utils;
import de.code_notes.backend.services.AppUserService;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;

 
@SpringBootApplication
@EnableAsync
@EnableScheduling
@Log4j2
public class CodeNotesBackendApplication {

    // @Autowired
    // private AppUserService appUserService;


    /**
     * NOTE: When adding methods that complement the environment, make sure to add those methods to the 
     * {@code CodeNotesBackendApplicationTest.init()} method too.
     *  
     * @param args
     */
	public static void main(String[] args) {
        readEnvFiles("./.env.version", !"production".equals(System.getenv("ENV")) ? "./.env.secrets" : "");

		SpringApplication.run(CodeNotesBackendApplication.class, args);
	}


    /**
     * Executed after {@code SpringApplication.run()} is completely done. At this point all beans and dependencies are injected / initialized.<p>
     * 
     * NOTE: Annotations like {@code @Value} or {@code @Autowired} work in here
     */
    @EventListener(ApplicationReadyEvent.class)
    public void postStartUp() {

        log.info("Registering default admin user");
        // this.appUserService.registerDefaultAdminUser();
    }


    /**
     * Read other .env files and set key values as sys properties. 
     * Wont override values from the actual .env file.<p>
     * 
     * Wont throw if an arg is blank.
     * 
     * @param envFileNames relative to root folder (same level as /src)
     */
    public static void readEnvFiles(@Nullable String ...envFileNames) {

        if (envFileNames == null || envFileNames.length == 0)
            return;

        for (String envFileName : envFileNames)
            readEnvFile(envFileName);
    }


    /**
     * Blank values are interpreted as {@code null}
     * 
     * @param fileName relative to root folder (same level as /src)
     */
    public static void readEnvFile(@Nullable String fileName) {

        if (isBlank(fileName))
            return;
        
        log.info(String.format("Reading '%s'...", fileName));
        
        try {
            Utils.readEnvFile(fileName)
                .entrySet()
                .forEach(entry -> 
                    System.setProperty(entry.getKey(), entry.getValue()));

        } catch (IOException e) {
            log.warn(String.format("Failed to read env file '%s': %s", fileName, e.getMessage() == null ? "<no message>" : e.getMessage()));
        }
    }
}