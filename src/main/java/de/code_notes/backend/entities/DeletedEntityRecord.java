package de.code_notes.backend.entities;

import de.code_notes.backend.abstracts.AbstractEntity;
import de.code_notes.backend.abstracts.NeedsDeletionRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Entity containing a hashed unique identifier of any entity serving as a deletion record. {@code created} is
 * representing the time of deletion.
 * 
 * @since 0.0.1
 */
@Entity
@Getter
@NoArgsConstructor
public class DeletedEntityRecord extends AbstractEntity {
    
    @Column(updatable = false, nullable = false)
    @NotBlank(message = "'uniqueIdHash' cannot be blank")
    private String uniqueIdHash;

    @Column(updatable = false, nullable = false)
    @NotBlank(message = "'className' cannot be blank")
    private String className;


    public DeletedEntityRecord(String uniqueIdHash, NeedsDeletionRecord deletedEntity) {

        this.uniqueIdHash = uniqueIdHash;
        this.className = deletedEntity.getDeletedEntityClassName();
    }
}