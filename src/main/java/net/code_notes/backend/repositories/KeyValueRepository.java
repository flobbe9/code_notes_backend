package net.code_notes.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.entities.KeyValue;

/**
 * @since 1.1.0
 */
@Repository
public interface KeyValueRepository extends AbstractRepository<KeyValue> {

    boolean existsBy_key(String get_key);

    boolean existsBy_keyAndIdNotIn(String get_key, List<Long> ids);

    Optional<KeyValue> findBy_key(String key);
}
