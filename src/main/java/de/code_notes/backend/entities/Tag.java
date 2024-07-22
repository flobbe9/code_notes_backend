package de.code_notes.backend.entities;

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
import jakarta.persistence.ManyToOne;
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
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "'name' cannot be blank")
    @Size(max = 50, message = "'name' cannot have more charactes than 50")
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private AppUser appUser;
}