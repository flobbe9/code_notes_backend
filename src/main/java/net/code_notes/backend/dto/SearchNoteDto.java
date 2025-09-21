package net.code_notes.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wrapper for note search jpa query results. Contains only the fields needed for note search to improove performance. Make sure
 * to use the exact Note field names for this to work with jpa.
 * 
 * @since latest
 */
public interface SearchNoteDto {

    Long getId();

    LocalDateTime getCreated();
    
    String getTitle();

    List<SearchNoteInputDto> getNoteInputs();
}
