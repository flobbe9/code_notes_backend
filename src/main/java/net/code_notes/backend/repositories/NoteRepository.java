package net.code_notes.backend.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.entities.Note;


/**
 * Interface defining the repository for the {@link Note} entity.
 * 
 * @since 0.0.1
 */
@Repository
public interface NoteRepository extends AbstractRepository<Note> {

    List<Note> findAllByAppUserOauth2IdOrderByCreatedDesc(String oauth2Id);
    List<Note> findAllByAppUserEmailOrderByCreatedDesc(String email);

    List<Note> findByAppUserOauth2IdOrderByCreatedDesc(String oauth2Id, Pageable pageable);
    List<Note> findByAppUserEmailOrderByCreatedDesc(String email, Pageable pageable);

    long countByAppUserOauth2Id(String oauth2Id);
    long countByAppUserEmail(String email);
}