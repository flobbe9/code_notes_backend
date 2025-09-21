package net.code_notes.backend.repositories;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.dto.SearchNoteJpaDto;
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
    List<Note> findByAppUserEmailAndTags_NameInOrderByCreatedDesc(String email, List<String> tagNames, Pageable pageable);

    /** Specifically for search note function */
    List<SearchNoteJpaDto> findByAppUserEmailAndTags_NameIn(String email, List<String> tagNames);
    List<SearchNoteJpaDto> findByAppUserEmail(String email);
    
    long countByAppUserOauth2Id(String oauth2Id);
    long countByAppUserEmail(String email);
    long countByAppUserEmailOrderByCreatedDesc(String email, PageRequest pageRequest);
    long countByAppUserEmailAndTags_NameInOrderByCreatedDesc(String email, List<String> tagNames,
            PageRequest pageRequest);
}
