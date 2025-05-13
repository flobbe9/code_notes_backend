package net.code_notes.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import net.code_notes.backend.entities.Note;
import net.code_notes.backend.services.NoteService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @since 0.0.1
 */
@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    
    @GetMapping("/get-all-by-appUser")
    @Operation(
        description = "Gets all notes related to app user currently logged in. Uses default order 'created' - 'descending'. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Got a logged in app user and returned their notes (may be empty)."),
            @ApiResponse(responseCode = "401", description = "Not logged in")
        }
    )
    public Flux<Note> getAllByAppUser() {

        return Flux.fromIterable(this.noteService.getAllByCurrentAppUser());
    }
    
    
    @GetMapping("/get-by-app_user-pageable")
    @Operation(
        description = "Gets a page of notes related to app user currently logged in. Uses default order 'created' - 'descending'. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Got a logged in app user and returned their notes (may be empty)."),
            @ApiResponse(responseCode = "401", description = "Not logged in")
        }
    )
    public Flux<Note> getByAppUserPageable(@RequestParam @Min(0) int pageNumber, @RequestParam @Min(1) int pageSize) {

        // read them params

        return Flux.fromIterable(this.noteService.getByCurrentAppUserOrderByCreatedDescPageable(pageNumber, pageSize));
    }


    @GetMapping("/count-by-app_user")
    @Operation(
        description = "Returns the total number of notes of the current app user. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Got the count"),
            @ApiResponse(responseCode = "401", description = "Not logged in")
        }
    )
    public Mono<Long> countAll() {

        return Mono.just(this.noteService.countByCurrentAppUser());
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


    @PostMapping("/save-all")
    @Operation(
        description = "Save or update notes and their relations. Keep the order of incoming notes!. AuthRequirements: LOGGED_IN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Saved or updated notes and their relations successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid param ('notes' may be empty but not null)"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Invalid csrf"),
            @ApiResponse(responseCode = "500", description = "Any other error")
        }
    )
    public Flux<Note> saveAll(@RequestBody List<@Valid Note> notes) throws IllegalArgumentException, ResponseStatusException {

        return Flux.fromIterable(this.noteService.saveAll(notes));
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