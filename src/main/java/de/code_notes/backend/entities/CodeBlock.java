package de.code_notes.backend.entities;

import de.code_notes.backend.abstracts.AbstractBlock;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity defining a code block inside a note. Will be highlighted in frontend. Extedns {@link AbstractBlock}
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeBlock extends AbstractBlock {
    
    @Nullable
    private String programmingLanguage;
}