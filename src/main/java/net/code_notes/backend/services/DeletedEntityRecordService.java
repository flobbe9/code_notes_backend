package net.code_notes.backend.services;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static net.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import net.code_notes.backend.abstracts.AbstractService;
import net.code_notes.backend.abstracts.NeedsDeletionRecord;
import net.code_notes.backend.entities.DeletedEntityRecord;
import net.code_notes.backend.repositories.DeletedEntityRecordRepository;


/**
 * @since 0.0.1
 */
@Service
public class DeletedEntityRecordService extends AbstractService<DeletedEntityRecord> {

    @Autowired
    private DeletedEntityRecordRepository deletedEntityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    protected DeletedEntityRecord saveNew(DeletedEntityRecord entity) throws ResponseStatusException, IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(entity);

        validateAndThrow(entity);

        if (this.deletedEntityRepository.exists(Example.of(entity)))
            throw new ResponseStatusException(CONFLICT, "'entity' already exists. Use this method only for saving new entities");

        entity = this.deletedEntityRepository.save(entity);

        return entity;
    }


    /**
     * Always throws {@code ResponseStatusException} 501, deleted entities are not to be updated.
     */
    @Override
    protected DeletedEntityRecord update(DeletedEntityRecord entity) throws ResponseStatusException, IllegalArgumentException {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Cannot update 'deletedEntities'");
    }


    /**
     * Save new {@link DeletedEntityRecord} for given {@code deletedEntity}. Will encrypt {@code deletedEntity.getUniqueId()}.
     * 
     * @param deletedEntity to create record for. Needs meerly it's uniqueId, no other fields need to be present at this point
     * @return the record (never {@code null})
     * @throws IllegalArgumentException
     * @throws IllegalStateException if {@code deletedEntity.getUniqueId()} is blank
     */
    public DeletedEntityRecord saveFor(NeedsDeletionRecord deletedEntity) throws IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(deletedEntity);

        String uniqueId = deletedEntity.getUniqueId();

        if (isBlank(uniqueId))
            throw new IllegalStateException("'uniqueId' cannot be blank");

        uniqueId = this.passwordEncoder.encode(uniqueId);

        return save(new DeletedEntityRecord(uniqueId, deletedEntity));
    }


    /**
     * Use {@link PasswordEncoder} bean to generate a hash for given {@code uniqueId}
     * 
     * @param uniqueId to generate has for (not altered)
     * @return the hashed {@code uniqueId} (never null)
     * @throws IllegalArgumentException
     */
    public String hashUniqueId(String uniqueId) throws IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(uniqueId);

        return this.passwordEncoder.encode(uniqueId);
    }


    /**
     * Loads the latest deletion record of an entity with given class and matching unique id or throws 404.<p>
     * 
     * Note that not finding a record does not imply anything about the existence of this entity.
     * 
     * @param uniqueId to match agains db entries
     * @param className of the deleted entity
     * @return the deletion-record  (never {@code null})
     * @throws IllegalArgumentException
     * @throws ResponseStatusException 404 if ther's no record of this entity ever beeing deleted
     */
    public DeletedEntityRecord loadByEntity(String uniqueId, String className) throws IllegalArgumentException, ResponseStatusException {

        assertArgsNotNullAndNotBlankOrThrow(uniqueId, className);

        return this.deletedEntityRepository.findByClassNameOrderByCreatedDesc(className)
            .stream()
            .filter(deletedEntityRecord -> 
                this.passwordEncoder.matches(uniqueId, deletedEntityRecord.getUniqueIdHash()))
            .findFirst()
            .orElseThrow(() -> 
                new ResponseStatusException(NOT_FOUND, "This entity has never been deleted by this application. Note that this does not imply anything about the acutal existence of this entity."));
    }
}