package de.code_notes.backend.repositories;

import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.CodeBlock;
import de.code_notes.backend.entities.PlainTextBlock;


/**
 * @since 0.0.1
 */
@Repository
public interface PlainTextBlockRepository extends AbstractRepository<PlainTextBlock> {
    
}