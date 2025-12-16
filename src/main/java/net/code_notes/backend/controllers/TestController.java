package net.code_notes.backend.controllers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.repositories.NoteRepository;
import net.code_notes.backend.services.AppUserService;

// import org.apache.log4j.Layout;


@RestController
@RequestMapping("/test")
@RequestScope
@Log4j2
public class TestController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AppUserService appUserService;

    private String cached;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    
    @GetMapping("/set")
    public void set(@RequestParam("cached") String cached) {
        // this.cached = cached;
        // this.testService.setCached(cached); 
    }

        
    @PostMapping("/get")
    public void get(@RequestParam("test") String test) {
        System.out.println(test);
        // log.info(this.noteRepository.findByAppUserEmail("florin.schikarski@outlook.com").get(0).getNoteInputs().get(0).getId());
        // log.info(this.noteRepository.findByAppUserEmailAndTags_NameIn("user@user.com", List.of("test")).get(0).getTitle());
        // log.info(this.noteRepository.findByAppUserEmail("florin.schikarski@outlook.com").getLast().getNoteInputs());
    }

    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    public void uploadFile(@RequestParam("picture") @NotNull(message = "Failed to upload. 'file' cannot be null.") MultipartFile picture) {
        try (OutputStream fos = new FileOutputStream(picture.getOriginalFilename())) {
            fos.write(picture.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}