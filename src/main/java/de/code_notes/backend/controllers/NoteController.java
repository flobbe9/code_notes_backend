package de.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.code_notes.backend.entities.Note;
import de.code_notes.backend.services.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    
    @GetMapping("/getAllByAppUser")
    @Operation(
        responses = {
            @ApiResponse(responseCode = "200", description = "Got a logged in app user and returned their notes (may be empty). AuthRequirements: LOGGED_IN"),
            @ApiResponse(responseCode = "401", description = "Not logged in")
        }
    )
    public Flux<Note> getAllByAppUser() {

        return Flux.fromIterable(this.noteService.getAllByCurrentAppUser());
    }


    @PostMapping("/save")
    @Operation(
        description = "Save or update note and relations. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Saved or updated note and relations successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid note"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
            @ApiResponse(responseCode = "500", description = "Note is null")
        }
    )
    public Mono<Note> save(@RequestBody @Valid Note note) {

        return Mono.just(this.noteService.save(note));
    }

    
    @DeleteMapping("/delete")
    @Operation(
        description = "Delete note. Will delete orphan tags as well. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Note deleted or did not exist anyway"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
        }
    )
    public void delete(@RequestParam Long id) {

        this.noteService.delete(id);
    }
}