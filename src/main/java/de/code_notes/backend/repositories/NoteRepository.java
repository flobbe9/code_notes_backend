package de.code_notes.backend.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.Note;


/**
 * Interface defining the repository for the {@link Note} entity.
 * 
 * @since 0.0.1
 */
@Repository
public interface NoteRepository extends AbstractRepository<Note> {

    List<Note> findAllByAppUserEmail(String appUserEmail);
}