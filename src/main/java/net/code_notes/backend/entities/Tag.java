package net.code_notes.backend.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.code_notes.backend.abstracts.AbstractEntity;


/**
 * Entity defining a tag a note can have to improove searching and sorting. Use only {@code name} and {@code appUser} for equals and hashcode.
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
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Tag extends AbstractEntity {

    private static final int NAME_MAX_LENGTH = 50;
    
    @Column(nullable = false, length = NAME_MAX_LENGTH)
    @NotBlank(message = "'name' cannot be blank")
    @Size(max = NAME_MAX_LENGTH, message = "'name' cannot have more charactes than " + NAME_MAX_LENGTH)
    @EqualsAndHashCode.Include
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
    @EqualsAndHashCode.Include
    private AppUser appUser;
}