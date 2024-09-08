package de.code_notes.backend.repositories;

import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.NoteInput;


/**
 * @since 0.0.1
 */
@Repository
public interface NoteInputRepository extends AbstractRepository<NoteInput> {
    
}