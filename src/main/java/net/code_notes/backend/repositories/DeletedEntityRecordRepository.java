package net.code_notes.backend.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.code_notes.backend.abstracts.AbstractRepository;
import net.code_notes.backend.entities.DeletedEntityRecord;


/**
 * @since 0.0.1
 */
@Repository
public interface DeletedEntityRecordRepository extends AbstractRepository<DeletedEntityRecord> {

    List<DeletedEntityRecord> findByClassNameOrderByCreatedDesc(String className);
}