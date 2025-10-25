package net.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

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

    
    @GetMapping("/set")
    public void set(@RequestParam String cached) {
        // this.cached = cached;
        // this.testService.setCached(cached); 
    }

        
    @GetMapping("/get")
    public void get() {
        // log.info(this.noteRepository.findByAppUserEmail("florin.schikarski@outlook.com").get(0).getNoteInputs().get(0).getId());
        // log.info(this.noteRepository.findByAppUserEmailAndTags_NameIn("user@user.com", List.of("test")).get(0).getTitle());
        // log.info(this.noteRepository.findByAppUserEmail("florin.schikarski@outlook.com").getLast().getNoteInputs());
    }
}