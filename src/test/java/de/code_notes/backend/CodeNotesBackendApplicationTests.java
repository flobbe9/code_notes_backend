package de.code_notes.backend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodeNotesBackendApplicationTests {

    @BeforeAll
    static void init() {
        // call this first
        CodeNotesBackendApplication.readAppEnvFile();

        CodeNotesBackendApplication.readEnvSecretsFile();
    }

	@Test
	void contextLoads() {
	}

}
