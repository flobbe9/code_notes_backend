package de.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.code_notes.backend.entities.Note;
import de.code_notes.backend.services.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    
    @GetMapping("/getAll")
    // TODO: add @HasRole
        // add nullcheck?
    public Flux<Note> getAll(@AuthenticationPrincipal UserDetails userDetails) {

        return Flux.fromIterable(this.noteService.getAllByUser(userDetails.getUsername()));
    }


    @PostMapping("/save")
    public Mono<Note> save(@RequestBody @Valid @NotNull(message = "'note' cannot be null") Note note) {

        // TODO: pass appuser
        return Mono.just(this.noteService.save(note));
    }

    
    @DeleteMapping("/delete")
    public void delete(@RequestParam Long id) {

        this.noteService.delete(id);
    }
}