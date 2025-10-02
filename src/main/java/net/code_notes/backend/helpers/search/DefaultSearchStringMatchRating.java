package net.code_notes.backend.helpers.search;

import net.code_notes.backend.abstracts.AbstractSearchStringMatchRating;

/**
 * Unspecific default implementation of {@code AbstractSearchStringMatchRating} simply adding the 
 * rating points to the total.
 * 
 * @since 1.0.0
 */
public class DefaultSearchStringMatchRating extends AbstractSearchStringMatchRating {

    public DefaultSearchStringMatchRating(double points) {
        super(points);
    }
    
    @Override
    protected double accumulatePoints(double totalPoints) {
        return totalPoints + this.points;
    }
}
