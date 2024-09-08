package de.code_notes.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.code_notes.backend.abstracts.AbstractEntity;
import de.code_notes.backend.abstracts.NoteInputType;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
    
    @Lob
    @Column(nullable = false, length = 50_000)
    @NotNull(message = "'value' cannot be null (but blank though)")
    private String value;
        
    @Nullable
    private String programmingLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "'type' cannot be null")
    private NoteInputType type;
    
    @ManyToOne
    @JsonIgnore
    private Note note;
}