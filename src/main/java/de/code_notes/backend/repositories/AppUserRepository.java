package de.code_notes.backend.repositories;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.AppUser;


/**
 * @since 0.0.1
 */
@Repository
public interface AppUserRepository extends AbstractRepository<AppUser> {
    
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}