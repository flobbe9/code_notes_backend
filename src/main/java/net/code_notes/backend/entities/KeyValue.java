package net.code_notes.backend.entities;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.code_notes.backend.abstracts.AbstractEntity;

/**
 * General key value entry for any miscellaneous data.<p>
 * 
 * Use {@code _key} for equals and hashcode
 * 
 * @since 1.1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class KeyValue extends AbstractEntity {
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "'_key' cannot be blank")
    @EqualsAndHashCode.Include
    private String _key;

    @Nullable
    private String _value;

    @Override
    public String toString() {
        return "{%s: %s}".formatted(this._key, this._value);
    }
}
