package de.code_notes.backend.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.Tag;


/**
 * Interface defining the repository for the {@link Tag} entity.
 * 
 * @since 0.0.1
 */
@Repository
public interface TagRepository extends AbstractRepository<Tag> {
    
    Optional<Tag> findByName(String name);
}