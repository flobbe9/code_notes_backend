package net.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @since 0.0.2
 */
@RestController
public class RootController {

    @Value("${VERSION}")
    private String VERSION;

    
    @GetMapping("/version")
    public String version() {

        return this.VERSION;
    }
}