package net.code_notes.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.helpers.Utils;


@SpringBootTest
@Log4j2
class CodeNotesBackendApplicationTests {

    @Value("${ENV}")
    private String ENV;
    

    @BeforeAll
    static void init() {
        log.info("Running tests in CI mode: {}", Utils.isCI());

        CodeNotesBackendApplication.readEnvFiles(
            "./.env.version",
            "./.env.pipeline",
            "./.env.secrets"
        );    

        if (Utils.isCI())
            // use h2 db in pipeline for simplicity
            System.setProperty("spring.datasource.url", "jdbc:h2:mem:cidb");
    }
    
	@Test
	void contextLoads() {
	}


    @Test
    void isLogLevelValid() {
        assertFalse("production".equals(this.ENV) && log.isDebugEnabled(), "Cannot log at DEBUG level while in production environment");
    }
}