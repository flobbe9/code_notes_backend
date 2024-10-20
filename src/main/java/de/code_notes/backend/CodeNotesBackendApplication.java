package de.code_notes.backend;

import static de.code_notes.backend.helpers.Utils.isBlank;

import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import de.code_notes.backend.helpers.Utils;
import jakarta.annotation.Nullable;
import lombok.extern.log4j.Log4j2;


@SpringBootApplication
@EnableAsync
@Log4j2
public class CodeNotesBackendApplication {

    /**
     * NOTE: When adding methods that complement the environment, make sure to add those methods to the 
     * {@code CodeNotesBackendApplicationTest.init()} method too.
     *  
     * @param args
     */
	public static void main(String[] args) {
        String ENV = System.getenv("ENV");

        readEnvFiles("./.env.version", !"prod".equals(ENV) ? "./.env.secrets" : "");

		SpringApplication.run(CodeNotesBackendApplication.class, args);
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
            log.warn(String.format("Failed to read env file '%s': %s"), fileName, e.getMessage());
        }
    }
}