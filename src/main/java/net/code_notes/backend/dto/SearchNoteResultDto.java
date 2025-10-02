package net.code_notes.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.code_notes.backend.entities.Note;

/**
 * Expected response object when returning note search results.
 * 
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
public class SearchNoteResultDto {
    /** The paginated search results */
    private List<Note> results;
    /** The total number of search results */
    private long totalResults;
}
