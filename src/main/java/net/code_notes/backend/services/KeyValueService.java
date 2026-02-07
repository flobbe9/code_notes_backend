package net.code_notes.backend.services;

import static net.code_notes.backend.helpers.Utils.assertArgsNotNullAndNotBlankOrThrow;
import static net.code_notes.backend.helpers.Utils.isBlank;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.log4j.Log4j2;
import net.code_notes.backend.abstracts.AbstractService;
import net.code_notes.backend.entities.KeyValue;
import net.code_notes.backend.repositories.KeyValueRepository;

@Service
@Log4j2
public class KeyValueService extends AbstractService<KeyValue> {
    
    @Autowired
    private KeyValueRepository keyValueRepository;
    

    @Override
    protected KeyValue saveNew(KeyValue entity) throws ResponseStatusException, IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(entity);

        validateAndThrow(entity);

        return this.keyValueRepository.save(entity);
    }

    @Override
    protected KeyValue update(KeyValue entity) throws ResponseStatusException, IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(entity);

        validateAndThrow(entity);

        return this.keyValueRepository.save(entity);
    }

    @Override
    protected boolean validateAndThrow(KeyValue entity) {
        super.validateAndThrow(entity);

        // unique check
        if (entity.getId() == null) {
            if (this.keyValueRepository.existsBy_key(entity.get_key()))
                throw new ResponseStatusException(CONFLICT, "Duplicate keyValue with _key '%s'".formatted(entity.get_key()));

        } else {
            if (this.keyValueRepository.existsBy_keyAndIdNotIn(entity.get_key(), List.of(entity.getId())))
                throw new ResponseStatusException(CONFLICT, "Duplicate keyValue with _key '%s'".formatted(entity.get_key()));
        }

        return true;
    }

    public Optional<KeyValue> loadByKey(String key) {
        if (isBlank(key))
            return Optional.ofNullable(null);
        
        return this.keyValueRepository.findBy_key(key);
    }
}
