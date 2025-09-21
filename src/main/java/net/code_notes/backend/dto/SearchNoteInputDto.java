package net.code_notes.backend.dto;

import net.code_notes.backend.abstracts.NoteInputType;

/**
 * Wrapper for noteInput search jpa query results. Contains only the fields needed for note search to improove performance. Make sure
 * to use the exact Note field names for this to work with jpa.
 * 
 * @since latest
 */
public interface SearchNoteInputDto {

    Long getId();

    NoteInputType getType();
}
