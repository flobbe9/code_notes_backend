package de.code_notes.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.code_notes.backend.entities.PlainTextBlock;
import de.code_notes.backend.repositories.PlainTextBlockRepository;


/**
 * @since 0.0.1
 */
@Service
public class PlainTextBlockService {

    @Autowired
    private PlainTextBlockRepository plainTextBlockRepository;

        
    public void deleteAll(List<PlainTextBlock> plainTextBlocks) {

        if (plainTextBlocks == null)
            return;

        this.plainTextBlockRepository.deleteAll(plainTextBlocks);
    }
}