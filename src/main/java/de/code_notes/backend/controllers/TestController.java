package de.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.Note;
import de.code_notes.backend.repositories.NoteRepository;
import de.code_notes.backend.services.AppUserService;


@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AppUserService appUserService;

    
    @PostMapping
    public Note test(@RequestBody Note note) {

        note.setAppUser(this.appUserService.getCurrent());

        // note = this.noteRepository.save(note);

        // if (note.getNoteInputs() != null) 
        //     for (NoteInput noteInput : note.getNoteInputs()) 
        //         noteInput.setNote(note);
        
        note = this.noteRepository.save(note);

        return note;
    }

        
    @GetMapping
    public AppUser test() {

        return new AppUser();
    }
}