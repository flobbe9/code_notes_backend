package net.code_notes.backend.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Meant for entities that contain sensitive data and therefore need a db record when beeing deleted.
 * 
 * @since 0.0.1
 */
public interface NeedsDeletionRecord {
    
    /**
     * May be used to check wether an entity instance has been deleted or not. Will be hashed and saved as {@code DeletedEntityRecord}.
     * 
     * @return a unique identifier of the entity (don't use db id)
     */
    @JsonIgnore
    String getUniqueId();


    /**
     * Use this method to save the class name to db.
     * 
     * @return the full package name plus the class name of the entity
     */
    @JsonIgnore
    default String getDeletedEntityClassName() {

        return this.getClass().getName();
    }
}