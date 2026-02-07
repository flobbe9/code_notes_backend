package net.code_notes.backend.repositories;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.abstracts.NoteInputType;
import net.code_notes.backend.dto.NoteInputValueJpaDto;
import net.code_notes.backend.entities.NoteInput;


/**
 * @since 0.0.1
 */
@Repository
public interface NoteInputRepository extends AbstractRepository<NoteInput> {

    NoteInputValueJpaDto getValueById(Long id);

    // TODO: remove later
    List<NoteInput> findAllByTypeIn(List<NoteInputType> of, PageRequest pageRequest);
}