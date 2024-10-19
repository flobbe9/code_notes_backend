package de.code_notes.backend;

import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import de.code_notes.backend.helpers.Utils;
import lombok.extern.log4j.Log4j2;


@SpringBootApplication
@EnableAsync
@Log4j2
public class CodeNotesBackendApplication {

	public static void main(String[] args) {
        // call this first
        readAppEnvFile();

        readEnvLocalFile();

		SpringApplication.run(CodeNotesBackendApplication.class, args);
	}


    /**
     * Read .app_env file from root folder (same level as /src) and set key values as sys properties. 
     * Wont override values from .env file.<p>
     * 
     * Blank values are interpreted as {@code null}
     */
    private static void readAppEnvFile() {
        
        log.info("Reading app env...");
        
        try {
            Utils.readEnvFile("./.app_env")
                .entrySet()
                .forEach(entry -> 
                    System.setProperty(entry.getKey(), entry.getValue()));

        } catch (IOException e) {
            log.warn("Failed to read env local file: " + e.getMessage());
        }
    }


    /**
     * Read .env.local file from root folder (same level as /src) and set key values as sys properties. 
     * Wont override values from .env file.<p>
     * 
     * Blank values are interpreted as {@code null}
     */
    private static void readEnvLocalFile() {

        String ENV = System.getenv("ENV");

        // case: is prod
        if ("prod".equals(ENV == null ? "" : ENV))
            return;

        log.info("Reading local env...");
        
        try {
            Utils.readEnvFile("./.env.local")
                .entrySet()
                .forEach(entry -> 
                    System.setProperty(entry.getKey(), entry.getValue()));

        } catch (IOException e) {
            log.warn("Failed to read env local file: " + e.getMessage());
        }
    }
}