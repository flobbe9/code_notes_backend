package de.code_notes.backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.Note;
import de.code_notes.backend.entities.PlainTextBlock;
import de.code_notes.backend.repositories.PlainTextBlockRepository;
import jakarta.annotation.Nullable;


/**
 * @since 0.0.1
 */
@Service
public class PlainTextBlockService {

    @Autowired
    private PlainTextBlockRepository plainTextBlockRepository;


    /**
     * Set given {@code note} to {@code note.plainTextBlocks} if not {@code null}.
     * 
     * @param note to update the plain text blocks {@code note} references for
     */
    public void addNoteReferences(@Nullable Note note) {

        if (note == null || note.getPlainTextBlocks() == null)
            return;

        note.getPlainTextBlocks().forEach(plainTextBlock -> plainTextBlock.setNote(note));
    }

        
    public boolean exists(@Nullable PlainTextBlock plainTextBlock) {

        if (plainTextBlock == null)
            return false;

        return this.plainTextBlockRepository.exists(Example.of(plainTextBlock));
    }


    public PlainTextBlock getById(@Nullable Long id) {

        if (id == null)
            return null;

        return this.plainTextBlockRepository
                .findById(id)
                .orElse(null);
    }


    /**
     * @param plainTextBlocks plain text blocks to be deleted
     */
    public void deleteAll(@Nullable List<PlainTextBlock> plainTextBlocks) {

        if (plainTextBlocks == null)
            return;

        this.plainTextBlockRepository.deleteAll(plainTextBlocks);
    }
}