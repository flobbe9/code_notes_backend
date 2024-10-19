package de.code_notes.backend.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.code_notes.backend.abstracts.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Entity defining a tag a note can have to improove searching and sorting. Use only {@code name} for equals and hashcode.
 * <p>
 * Is unique to an {@code appUser}.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tag extends AbstractEntity {
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "'name' cannot be blank")
    @Size(max = 50, message = "'name' cannot have more charactes than 50")
    private String name;

    @ManyToMany
    @JoinTable(
        name = "note_tags", 
        inverseJoinColumns = { 
            @JoinColumn(name = "note_id", referencedColumnName = "id", nullable = false) 
        }
    )
    @JsonIgnore
    private Set<Note> notes;

    @ManyToOne
    @JsonIgnore
    private AppUser appUser;
}