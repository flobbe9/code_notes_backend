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
    
    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmail(String email);

    Optional<AppUser> findByOauth2Id(String oauth2Id);

    boolean existsByOauth2Id(String oauth2Id);

    boolean existsByEmailAndOauth2IdIsNull(String email);

    Optional<AppUser> findByEmailAndOauth2IdIsNull(String email);
}