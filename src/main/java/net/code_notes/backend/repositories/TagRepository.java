package net.code_notes.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.Tag;


/**
 * Interface defining the repository for the {@link Tag} entity.
 * 
 * @since 0.0.1
 */
@Repository
public interface TagRepository extends AbstractRepository<Tag> {
    
    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameAndAppUser(String name, AppUser appUser);

    List<Tag> findAllByAppUser(AppUser appUser);

    @Transactional
    void deleteByNotesIsNull();

    boolean existsByNameAndAppUser(String name, AppUser appUser);
}