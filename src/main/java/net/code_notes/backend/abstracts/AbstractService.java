package net.code_notes.backend.abstracts;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;


/**
 * Abstract class for any "Service" class. Defines some helper methods any service class may need.
 * 
 * @since 0.0.1
 */
@Service
abstract public class AbstractService<T extends AbstractEntity> {

    @Autowired
    private Validator validator;
    
    /**
     * Save new or update given {@code entity} depending on whether it has an {@code id} or not.
     * 
     * @param entity to save
     * @return saved {@code entity}, never {@code null}
     * @throws ResponseStatusException if {@code entity} is invalid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     */
    public T save(T entity) throws ResponseStatusException, IllegalArgumentException {
        // case: falsy param
        if (entity == null)
            throw new IllegalArgumentException("Failed to save entity. 'entity' cannot be null");

        // case: entity exists
        if (entity.getId() != null)
            return update(entity);

        return saveNew(entity);
    }


    /**
     * Validate and save given {@code entity}. Assuming that it has no {@code id}.
     * 
     * @param entity to save (will be altered)
     * @return the saved {@code entity}, never {@code null}
     * @throws ResponseStatusException if {@code entity} is invalid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     */
    abstract protected T saveNew(T entity) throws ResponseStatusException, IllegalArgumentException;


    /**
     * Update and save given {@code entity}. Assuming that {@code id} is not {@code null}.
     * 
     * @param entity to update (will be altered)
     * @return the updated {@code entity}, never {@code null}
     * @throws ResponseStatusException if {@code entity} is invalid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     */
    abstract protected T update(T entity) throws ResponseStatusException, IllegalArgumentException;

    
    /**
     * Validate given {@code entity} using all annotations of it's entity. Throw if {@code entity} is invalid.
     * 
     * @param entity to validate
     * @return true if {@code entity} is valid
     * @throws IllegalArgumentException if {@code entity} is null
     * @throws ResponseStatusException if {@code entity} is invalid
     */
    protected boolean validateAndThrow(T note) {
        // case: falsy param
        if (note == null)
            throw new IllegalArgumentException("Failed to validate entity. 'entity' cannot be null");

        this.validator.validateObject(note)
                        .failOnError((message) -> new ResponseStatusException(BAD_REQUEST, "'entity' invalid"));

        return true;
    }
}