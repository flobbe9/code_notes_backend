package de.code_notes.backend.entities;

import java.util.HashSet;
import java.util.Set;

import de.code_notes.backend.abstracts.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Entity defining a tag a note can have to improove searching and sorting. Use only {@code name} for equals and hashcode.
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
    
    @Column(nullable = false, unique = true)
    @NotBlank(message = "'name' cannot be blank")
    @Size(max = 50, message = "'name' cannot have more charactes than 50")
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false)
    @NotNull(message = "'appUser' cannot be null")
    private AppUser appUser;
}