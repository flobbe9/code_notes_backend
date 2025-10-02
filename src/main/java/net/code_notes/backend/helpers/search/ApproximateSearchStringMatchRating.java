package net.code_notes.backend.helpers.search;

import net.code_notes.backend.abstracts.AbstractSearchStringMatchRating;
import net.code_notes.backend.helpers.Utils;

/**
 * @since 1.0.0
 */
public class ApproximateSearchStringMatchRating extends AbstractSearchStringMatchRating {

    public ApproximateSearchStringMatchRating() {
        super(Utils.SEARCH_APPROXIMATE_RATING_POINTS);
    }
    
    @Override
    protected double accumulatePoints(double totalPoints) {
        return totalPoints + this.points;
    }
}
