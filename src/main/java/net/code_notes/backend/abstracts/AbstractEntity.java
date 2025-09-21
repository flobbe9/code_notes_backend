package net.code_notes.backend.abstracts;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import net.code_notes.backend.helpers.Utils;


/**
 * @since 0.0.1
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;
    
    @Column(nullable = false)
    @JsonFormat(pattern = Utils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime created;

    @Column(nullable = false)
    @JsonFormat(pattern = Utils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime updated;


    /**
     * Copy all fields of {@link AbstractEntity} from given {@code abstractEntity} to {@code this}.
     * 
     * @param abstractEntity
     * @return {@code this}
     * @throws IllegalArgumentException
     */
    public AbstractEntity copyAbstractEntityFields(AbstractEntity abstractEntity) throws IllegalArgumentException {

        Utils.assertArgsNotNullAndNotBlankOrThrow(abstractEntity);

        this.id = abstractEntity.id;
        this.created = abstractEntity.created;
        this.updated = abstractEntity.updated;

        return this;
    }


    @PrePersist
    public void persist() {

        if (this.created == null)
            this.created = LocalDateTime.now();

        this.updated = LocalDateTime.now();
    }


    @PreUpdate
    public void update() {

        this.updated = LocalDateTime.now();
    }


    @Override 
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (other == this)
            return true;

        if (other.getClass() != this.getClass())
            return false;

        AbstractEntity entity = (AbstractEntity) other;

        if (this.id != null && entity.getId() != null)
            return this.id.equals(entity.getId());

        return this.id == null && entity.getId() == null;
    }


    @Override
    public int hashCode() {
        int hash = 7;

        hash = 31 * hash + (this.id == null ? 0 : this.id.intValue());
        hash = 31 * hash + this.getClass().hashCode();

        return hash;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " with id: " + this.id;
    }
}