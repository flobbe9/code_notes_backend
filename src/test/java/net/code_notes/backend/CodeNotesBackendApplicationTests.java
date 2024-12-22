package net.code_notes.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.CodeNotesBackendApplication;


@SpringBootTest
@Log4j2
class CodeNotesBackendApplicationTests {

    @Value("${ENV}")
    private String ENV;


    @BeforeAll
    static void init() {
        CodeNotesBackendApplication.readEnvFiles(
            "./.env.version",
            "./.env.secrets.pipeline",
            "./.env.secrets"
        );    
    }


	@Test
	void contextLoads() {
	}


    @Test
    void isLogLevelValid() {

        assertFalse("production".equals(this.ENV) && log.isDebugEnabled(), "Cannot log at DEBUG level while in production environment");
    }
}