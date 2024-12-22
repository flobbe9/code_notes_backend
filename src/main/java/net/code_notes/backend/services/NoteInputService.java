package net.code_notes.backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;
import net.code_notes.backend.entities.NoteInput;
import net.code_notes.backend.repositories.NoteInputRepository;


/**
 * @since 0.0.1
 */
@Service
public class NoteInputService {

    @Autowired
    private NoteInputRepository noteInputRepository;


        
    public boolean exists(@Nullable NoteInput noteInput) {

        if (noteInput == null)
            return false;

        return this.noteInputRepository.exists(Example.of(noteInput));
    }


    public NoteInput getById(@Nullable Long id) {

        if (id == null)
            return null;

        return this.noteInputRepository
                .findById(id)
                .orElse(null);
    }


    /**
     * @param noteInputs plain text blocks to be deleted
     */
    public void deleteAll(@Nullable List<NoteInput> noteInputs) {

        if (noteInputs == null)
            return;

        this.noteInputRepository.deleteAll(noteInputs);
    }
}