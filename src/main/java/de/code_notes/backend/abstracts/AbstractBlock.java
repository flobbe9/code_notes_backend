package de.code_notes.backend.abstracts;

import de.code_notes.backend.entities.Note;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Abstract class defining a default block inside a note. Use default equals and hash code.
 * 
 * @since 0.0.1
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbstractBlock extends AbstractEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Note note;
    
    @Lob
    @Column(nullable = false)
    @NotNull(message = "'value' cannot be null (but blank though)")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "'blockType' cannot be null")
    private BlockType blockType;
}
