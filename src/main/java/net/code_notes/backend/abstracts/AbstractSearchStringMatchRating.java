package net.code_notes.backend.abstracts;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.code_notes.backend.helpers.search.SearchStringUtils;

/**
 * Contains details about the priority of a search string match.
 * 
 * @since latest
 * @see {@link SearchStringUtils}
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public abstract class AbstractSearchStringMatchRating {

    protected double points;

    /**
     * Accumulate {@link #points} to {@code totalPoints}. This may be as simple as {@code totalPoints + points} but may
     * be any function depending on how important the match should be. E.g. do {@code totalPoints * points} in order to make this 
     * match a big effect on the final outcome.
     *  
     * @param totalPoints the current sum of search match rating points
     * @return the new {@code totalPoints} value
     */
    abstract protected double accumulatePoints(double totalPoints);
}
