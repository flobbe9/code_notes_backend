package de.code_notes.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.CodeBlock;
import de.code_notes.backend.repositories.CodeBlockRepository;


/**
 * @since 0.0.1
 */
@Service
public class CodeBlockService {

    @Autowired
    private CodeBlockRepository codeBlockRepository;
    

    public void deleteAll(List<CodeBlock> codeBlocks) {

        if (codeBlocks == null)
            return;

        this.codeBlockRepository.deleteAll(codeBlocks);
    }
}
