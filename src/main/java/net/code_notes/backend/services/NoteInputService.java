package net.code_notes.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;
import net.code_notes.backend.dto.NoteInputValueDto;
import net.code_notes.backend.entities.NoteInput;
import net.code_notes.backend.repositories.NoteInputRepository;


/**
 * @since 0.0.1
 */
@Service
public class NoteInputService {

    @Autowired
    private NoteInputRepository noteInputRepository;

    @Nullable
    public NoteInput loadById(@Nullable Long id) {
        if (id == null)
            return null;

        return this.noteInputRepository
            .findById(id)
            .orElse(null);
    }

    @Nullable
    public NoteInputValueDto loadValueById(@Nullable Long id) {
        if (id == null)
            return null;

        return this.noteInputRepository.getValueById(id);
    }
}