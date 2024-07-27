package de.code_notes.backend.entities;

import de.code_notes.backend.abstracts.AbstractBlock;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity defining a block inside a note with only plain text. Extends {@link AbstractBlock}.
 * Use default equals and hash code.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlainTextBlock extends AbstractBlock {
    
}