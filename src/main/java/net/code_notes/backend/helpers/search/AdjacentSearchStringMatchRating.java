package net.code_notes.backend.helpers.search;

import static net.code_notes.backend.helpers.Utils.SEARCH_ADJACENT_MATCH_RATING_POINTS;

import net.code_notes.backend.abstracts.AbstractSearchStringMatchRating;

/**
 * @since 1.0.0
 */
public class AdjacentSearchStringMatchRating extends AbstractSearchStringMatchRating {

    public AdjacentSearchStringMatchRating() {
        super(SEARCH_ADJACENT_MATCH_RATING_POINTS);
    }
    
    @Override
    protected double accumulatePoints(double totalPoints) {
        return totalPoints + this.points;
    }
    
}
