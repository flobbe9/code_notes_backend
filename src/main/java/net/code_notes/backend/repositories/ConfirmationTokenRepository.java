package net.code_notes.backend.repositories;

import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.entities.AppUser;
import net.code_notes.backend.entities.ConfirmationToken;

import java.time.LocalDateTime;
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

    @Transactional
    void deleteByCreatedBefore(LocalDateTime minusMonths);

    Optional<ConfirmationToken> findByAppUser(AppUser appUser);
}