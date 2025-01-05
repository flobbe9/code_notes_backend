package net.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.Note;
import net.code_notes.backend.repositories.NoteRepository;
import net.code_notes.backend.services.AppUserService;


@RestController
@RequestMapping("/test")
@SessionScope
public class TestController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AppUserService appUserService;

    private String cached;

    
    @GetMapping("/set")
    public void set(@RequestParam String cached) {

        this.cached = cached;
    }

        
    @GetMapping("/get")
    public void get() {

        System.out.println(this.cached);
    }
}