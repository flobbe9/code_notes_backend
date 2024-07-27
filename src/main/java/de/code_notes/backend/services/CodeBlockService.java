package de.code_notes.backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.CodeBlock;
import de.code_notes.backend.entities.Note;
import de.code_notes.backend.repositories.CodeBlockRepository;
import jakarta.annotation.Nullable;


/**
 * @since 0.0.1
 */
@Service
public class CodeBlockService {

    @Autowired
    private CodeBlockRepository codeBlockRepository;
    

    /**
     * Set given {@code note} to {@code note.codeBlocks} if not {@code null}.
     * 
     * @param note to update the code blocks {@code note} references for
     */
    public void addNoteReferences(@Nullable Note note) {

        if (note == null || note.getCodeBlocks() == null)
            return;

        note.getCodeBlocks().forEach(codeBlock -> codeBlock.setNote(note));
    }

        
    public boolean exists(@Nullable CodeBlock codeBlock) {

        if (codeBlock == null)
            return false;

        return this.codeBlockRepository.exists(Example.of(codeBlock));
    }


    public CodeBlock getById(@Nullable Long id) {

        if (id == null)
            return null;

        return this.codeBlockRepository
                .findById(id)
                .orElse(null);
    }
    

    /**
     * @param codeBlocks code blocks to be deleted
     */
    public void deleteAll(@Nullable List<CodeBlock> codeBlocks) {

        if (codeBlocks == null)
            return;

        this.codeBlockRepository.deleteAll(codeBlocks);
    }
}
