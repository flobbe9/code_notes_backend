package de.code_notes.backend.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.code_notes.backend.abstracts.AbstractEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity defining a note. Contains inputs and tags. Use default equals and hash code.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
public class Note extends AbstractEntity {

    private static final int TITLE_MAX_LENGTH = 255;
    
    @Column(nullable = false)
    @NotNull(message = "'title' cannot be null (but blank though)")
    @Size(max = TITLE_MAX_LENGTH, message = "'title' cannot have more charactes than " + TITLE_MAX_LENGTH)
    private String title;

    @OneToMany(
        cascade = { CascadeType.ALL },
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @JoinColumn(name = "note_id")
    @Nullable
    @OrderColumn
    private List<@Valid @NotNull(message = "'note.noteInput' cannot be null") NoteInput> noteInputs;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "note_tags", 
        inverseJoinColumns = { 
            @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false) 
        })
    @Nullable
    @OrderColumn
    private List<@Valid @NotNull(message = "'note.tag' cannot be null") Tag> tags;

    @ManyToOne
    @JsonIgnore
    private AppUser appUser;
}