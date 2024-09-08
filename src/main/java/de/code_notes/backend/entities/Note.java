package de.code_notes.backend.entities;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.code_notes.backend.abstracts.AbstractEntity;
import io.swagger.v3.oas.annotations.Hidden;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
    
    @Column(nullable = false)
    @NotNull(message = "'title' cannot be null (but blank though)")
    private String title;

    @OneToMany(
        mappedBy = "note",
        cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @Nullable
    private List<@Valid NoteInput> noteInputs;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "note_tags", inverseJoinColumns = { @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false) })
    @Nullable
    // TODO: make this a list
    private Set<@Valid Tag> tags;

    @ManyToOne
    @JsonIgnore
    private AppUser appUser;
}