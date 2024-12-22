package net.code_notes.backend.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.code_notes.backend.abstracts.AbstractEntity;
import net.code_notes.backend.abstracts.NoteInputType;


/**
 * Entity defining an input inside a note. Use default equals and hash code.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteInput extends AbstractEntity {

    private static final int VALUE_MAX_LENGTH = 65_535;
    
    @Lob
    @Column(nullable = false, length = VALUE_MAX_LENGTH)
    @NotNull(message = "'value' cannot be null (but blank though)")
    @Size(max = VALUE_MAX_LENGTH, message = "'value' cannot have more charactes than " + VALUE_MAX_LENGTH)
    private String value;
        
    @Nullable
    private String programmingLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "'type' cannot be null")
    private NoteInputType type;
}