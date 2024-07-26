package de.code_notes.backend.abstracts;

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
     * Validate given {@code entity} using all annotations of it's entity. Throw if {@code entity} is invalid.
     * 
     * @param entity to validate
     * @return true if {@code entity} is valid
     * @throws IllegalStateException if {@code entity} is null
     * @throws ResponseStatusException if {@code entity} is invalid
     */
    protected boolean validateAndThrow(T note) {

        // case: falsy param
        if (note == null)
            throw new IllegalStateException("Failed to validate entity. 'entity' cannot be null");

        this.validator.validateObject(note)
                        .failOnError((message) -> new ResponseStatusException(BAD_REQUEST, "'entity' invalid"));

        return true;
    }
}