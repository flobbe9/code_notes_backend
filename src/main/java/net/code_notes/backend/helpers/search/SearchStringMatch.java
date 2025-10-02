package net.code_notes.backend.helpers.search;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.code_notes.backend.abstracts.AbstractSearchStringMatchRating;

/**
 * Contains details about two strings matching for search.
 *  
 * @since 1.0.0
 * @see {@link SearchUtils}
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SearchStringMatch {

    /** Index of the word within the search phrase that was found a match for. */
    private int searchWordIndex;

    /** Length of the search word. */
    private int searchWordLength;

    /** Index of the word within the comparison phrase that matches with a search phrase word */
    private int compareWordIndex;

    /** 
     * Index of the first character of the matching compareWord. 
     * In case of an exact match the index would always be 0. In case of an approximate match the index represents
     * the start of the matching substring.<p>
     * 
     * E.g. {@code "searchInput"} matches {@code "theSearchInput"} at character index 3 
     */
    private int compareWordSubstringStartIndex;

    /** Represents how strong the search and comparison phrase match. Will determine the order in which the search results appear. */
    private AbstractSearchStringMatchRating rating;

    public SearchStringMatch(int compareWordSubstringStartIndex, AbstractSearchStringMatchRating rating) {
        this.compareWordSubstringStartIndex = compareWordSubstringStartIndex;
        this.rating = rating;
    }

    /**
     * Indicates that the search- and compareWords of this object don't actually match.
     * 
     * @return {@code true} if there's no {@code rating} or no {@code rating.points}
     */
    public boolean isNotAMatch() {
        return this.rating == null || this.rating.getPoints() == 0;
    }
}
