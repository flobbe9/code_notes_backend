package de.code_notes.backend.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.AppUser;
import jakarta.transaction.Transactional;


/**
 * @since 0.0.1
 */
@Repository
public interface AppUserRepository extends AbstractRepository<AppUser> {
    
    @Transactional
    void deleteByEmail(String email);

    Optional<AppUser> findByOauth2Id(String oauth2Id);

    Optional<AppUser> findByEmail(String email);

    boolean existsByOauth2Id(String oauth2Id);

    boolean existsByEmail(String email);
}