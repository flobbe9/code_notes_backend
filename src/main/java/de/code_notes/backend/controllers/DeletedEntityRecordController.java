package de.code_notes.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.code_notes.backend.entities.DeletedEntityRecord;
import de.code_notes.backend.services.DeletedEntityRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import reactor.core.publisher.Mono;


/**
 * @since 0.0.1
 */
@RestController
public abstract class DeletedEntityRecordController {
    
    @Autowired
    private DeletedEntityRecordService deletedEntityRecordService;


    /**
     * @return the class name of the deletion-record as stored in db
     * @see DeletedEntityRecord
     * @see NeedsDeletionRecord
     */
    abstract protected String getDeletedEntityClassName();


    @GetMapping("/get-deletion-record")
    @Operation(
        description = "Get the deletion-record of the entity with given unique id or throw 404. To get the uniqueId see the 'getUniqueId()' method of the 'NeedsDeletionRecord' entity. AuthRequirements: LOGGED_IN, ROLE_ADMIN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Found a deletion record"),
            @ApiResponse(responseCode = "401", description = "Not logged in"),
            @ApiResponse(responseCode = "403", description = "Missing permissions"),
            @ApiResponse(responseCode = "404", description = "Entity has never been deleted"),
        }
    )
    @Secured("ROLE_ADMIN")
    public Mono<DeletedEntityRecord> getDeletionRecord(@RequestParam @NotBlank(message = "'uniqueId' cannot be blank") String uniqueId) {
        
        return Mono.just(this.deletedEntityRecordService.loadByEntity(uniqueId, getDeletedEntityClassName()));
    }
}