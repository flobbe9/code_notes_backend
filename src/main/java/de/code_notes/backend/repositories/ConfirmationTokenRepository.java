package de.code_notes.backend.repositories;

import org.springframework.stereotype.Repository;

import de.code_notes.backend.abstracts.AbstractRepository;
import de.code_notes.backend.entities.AppUser;
import de.code_notes.backend.entities.ConfirmationToken;
import jakarta.transaction.Transactional;

import java.util.Optional;


/**
 * @since 0.0.1
 */
@Repository
public interface ConfirmationTokenRepository extends AbstractRepository<ConfirmationToken> {

    boolean existsByTokenAndAppUser(String token, AppUser appUser);

    @Transactional
    void deleteByAppUser(AppUser appUser);

    Optional<ConfirmationToken> findByToken(String token);
}