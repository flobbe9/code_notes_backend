package net.code_notes.backend.helpers.search;

import static net.code_notes.backend.helpers.Utils.assertArgsNullOrBlank;
import static net.code_notes.backend.helpers.Utils.isBlank;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.code_notes.backend.abstracts.AbstractSearchStringMatchRating;

/**
 * Contains helpers for searching and comparing strings.<p>
 * 
 * <h3>Glossary</h3><p>
 * <b>phrase:</b> A sequence of strings separated by whitespace.<p>
 * <b>word:</b> A string without whitespace, usually part of a phrase.<p>
 * <b>match:</b> When two words / phrases have some correlation such that the compareWord/-phrase should be treated as search result 
 * <ul>
 *  <li><b>exact match:</b>: Equals ignore case. Handle {@code null} gracefully sothat if both search- and compareWord are {@code null} it's considered an exact match as well.</li>
 *  <li><b>approximate match:</b>: Contains ignore case. Handle {@code null} gracefully sothat if both search- and compareWord are {@code null} it's considered an approximate match as well.</li>
 *  <li><b>adjacent match:</b>: Two search words directly next to eachother find matches in either two compare words directly next to eachother or two substrings in the same compare word directly or not directly next to 
 *      eachother (not overlapping though). Order needs to be the same. See {@link #isMatchAdjacent(SearchStringMatch, SearchStringMatch)}.
 *  </li>
 * </ul>
 * <b>searchWord / searchPhrase:</b> The one string to use for comparison, e.g. user input in a search bar<p>
 * <b>compareWord / comparePhrase:</b> One of possibly many strings the searchWord/-phrase is beeing compared to, e.g. a note title
 * <b>result: </b> A compareWord/-phrase that had at least one match and should be returned by the search function
 * 
 * @since 1.0.0
 */
public abstract class SearchStringUtils {

    /**
     * Asses how well the {@code searchPhrase} matches the {@code comparePhrase} by giving it a rating.<p>
     * 
     * <ul>
     *  <li>Both phrases are split into their words.</li>
     *  <li>Each search word can have one match at most. The match with the highest rating will be used.</li>
     *  <li>Additional rating points are added for every adjacent match a search word has.</li>
     * </ul> 
     * 
     * @param searchPhrase
     * @param comparePhrase
     * @return the rating points
     * @see {@link AbstractSearchStringMatchRating} implementations for the exact rating point amounts
     */
    public static double matchPhrases(@Nullable String searchPhrase, @Nullable String comparePhrase) {
        if (isBlank(searchPhrase) || isBlank(comparePhrase))
            return matchWords(searchPhrase, comparePhrase).getRating().getPoints();

        String[] searchWords = searchPhrase.split(" ");
        String[] compareWords = comparePhrase.split(" ");

        double totalRatingPoints = 0;    
        List<SearchStringMatch> prevSearchWordMatches = new LinkedList<>();
        
        for (int searchWordIndex = 0; searchWordIndex < searchWords.length; searchWordIndex++) {
            String searchWord = searchWords[searchWordIndex];

            // case: search contained multiple conscutive whtiespaces, don't match those
            if (isBlank(searchWord))
                continue;

            double currentSearchWordRatingPoints = 0;    
            List<SearchStringMatch> currentSearchWordMatches = new LinkedList<>();

            for (int compareWordIndex = 0; compareWordIndex < compareWords.length; compareWordIndex++) {
                String compareWord = compareWords[compareWordIndex];
                // case: search contained multiple conscutive whtiespaces, don't match those
                if (isBlank(compareWord))
                    continue;

                SearchStringMatch searchWordMatch = matchWords(searchWord, compareWord);
                searchWordMatch.setSearchWordLength(searchWord.length());
                searchWordMatch.setSearchWordIndex(searchWordIndex);
                searchWordMatch.setCompareWordIndex(compareWordIndex);
                
                if (searchWordMatch.isNotAMatch())
                    continue;

                currentSearchWordMatches.add(searchWordMatch);
                
                double searchWordRatingPoints = searchWordMatch.getRating().getPoints();
                if (searchWordRatingPoints > currentSearchWordRatingPoints)
                    currentSearchWordRatingPoints = searchWordRatingPoints;
            }

            currentSearchWordRatingPoints += accumulateAdjacentMatches(prevSearchWordMatches, currentSearchWordMatches);
            totalRatingPoints += currentSearchWordRatingPoints;
            prevSearchWordMatches = currentSearchWordMatches;
        }

        return totalRatingPoints;
    }

    /**
     * Check if args are a "match" and if so, rate the match and determine the character index in the {@code compareWord} where the match occured.
     *  
     * @param searchWord
     * @param compareWord
     * @return a new match object with only the {@code compareWordSubstringStartIndex} and the {@code rating}. Set
     * {@code compareWordSubstringStartIndex = -1} and {@code rating.points = 0} if no match
     */
    @NonNull
    public static SearchStringMatch matchWords(@Nullable String searchWord, @Nullable String compareWord) {
        AbstractSearchStringMatchRating rating = null;
        int compareWordSubstringStartIndex = 0;

        if (isExactMatch(searchWord, compareWord))
            rating = new ExactSearchStringMatchRating();

        else if (isApproximateMatch(searchWord, compareWord)) {
            rating = new ApproximateSearchStringMatchRating();
            compareWordSubstringStartIndex = Strings.CI.indexOf(compareWord, searchWord);
        }

        // case: no match, return neutral instance
        if (rating == null) {
            compareWordSubstringStartIndex = -1;
            rating = new DefaultSearchStringMatchRating(0);
        }

        return new SearchStringMatch(compareWordSubstringStartIndex, rating);
    }

    /**
     * Indicates that the match should get more rating points.
     * 
     * @param searchWord
     * @param compareWord
     * @return {@code searchWord.equalsIgnoreCase(compareWord)}
     */
    public static boolean isExactMatch(@Nullable String searchWord, @Nullable String compareWord) {
        return Strings.CI.equals(searchWord, compareWord);
    }

    /**
     * Indicates that the match should get less rating points.
     * 
     * @param searchWord
     * @param compareWord
     * @return {@code compareWord.containsIgnoreCase(searchWord)}
     */
    public static boolean isApproximateMatch(@Nullable String searchWord, @Nullable String compareWord) {
        return Strings.CI.contains(compareWord, searchWord);
    }

    /**
     * See class comment for detailed summary.<p>
     * 
     * <h3>Examples:</h3>
     * <i>Search:</i> "<b>autostart</b> <b>configurat</b>" - <i>matches:</i> "linux <b>autostart</b> <b>configurat</b>ion"<p>
     * <i>Search:</i> "<b>autostart</b> <b>configuration</b>" - <i>matches:</i> "linux <b>autostart</b> <b>configuration</b>"<p>
     * <i>Search:</i> "<b>autostart</b> <b>configuration</b>" - <i>not adjacent:</i> "autostart linux configuration"<p>
     * <i>Search:</i> "<b>autostart</b> <b>configuration</b>" - <i>wrong order:</i> "configuration autostart"<p>
     * <i>Search not adjacent:</i> "autostart linux configurat" - <i>adjacent:</i> "<b>autostart</b> <b>configuration</b>"<p>
     * 
     * <i>Search:</i> "<b>autostart</b> <b>configurat</b>" - <i>same word</i>: "linux <b>autostartconfigurat</b>ion"<p>
     * <i>Search:</i> "<b>autostart</b> <b>configurat</b>" - <i>same word, consider this adjacent</i>: "linux <b>autostart</b>Linux<b>configurat</b>ion"<p>
     * <i>Search:</i> "<b>autostart</b> <b>configurat</b>" - <i>same word, wrong order</i>: "linux configurationLinuxautostart"<p>
     * 
     * @param leftSearchWordMatch the match detail for the left search word of the two search words beeing compared
     * @param rightSearchWordMatch the match detail for the right search word of the two search words beeing compared
     * @return {@code true} if matches are adjacent (see comment above), {@code false} if not or if at least one arg is {@code null}
     */
    public static boolean isMatchAdjacent(@Nullable SearchStringMatch leftSearchWordMatch, @Nullable SearchStringMatch rightSearchWordMatch) {
        if (assertArgsNullOrBlank(leftSearchWordMatch, rightSearchWordMatch))
            return false;

        // case: search words not adjacent or in wrong order
        if (rightSearchWordMatch.getSearchWordIndex() - leftSearchWordMatch.getSearchWordIndex() != 1)
            return false;

        // case: match for same compare word
        if (leftSearchWordMatch.getCompareWordIndex() == rightSearchWordMatch.getCompareWordIndex())
            // left compareWord is left and does not overlap right compareWord
            return leftSearchWordMatch.getCompareWordSubstringStartIndex() + leftSearchWordMatch.getSearchWordLength() - 1 < rightSearchWordMatch.getCompareWordSubstringStartIndex();

        // case: match for different compare word, not adjacent or in wrong order
        if (rightSearchWordMatch.getCompareWordIndex() - leftSearchWordMatch.getCompareWordIndex() != 1)
            return false;

        // case: match for different adjacent compare words in correct order 
        return true;
    }

    /**
     * Count the number of adjacent matches and return the accumulated rating points using {@link AdjacentSearchStringMatchRating}.<p>
     * 
     * @param leftSearchWordMatches
     * @param rightSearchWordMatches
     * @return the total rating points for all adjacent match or 0 if no adjacent matches at all or at least on arg is {@code null}
     * @see #isMatchAdjacent(SearchStringMatch, SearchStringMatch)
     */
    private static double accumulateAdjacentMatches(@Nullable List<SearchStringMatch> leftSearchWordMatches, @Nullable List<SearchStringMatch> rightSearchWordMatches) {
        if (assertArgsNullOrBlank(leftSearchWordMatches, rightSearchWordMatches))
            return 0;

        AdjacentSearchStringMatchRating searchStringMatchRating = new AdjacentSearchStringMatchRating();
        double ratingPoints = 0;

        for (SearchStringMatch leftSearchWordMatch : leftSearchWordMatches)
            for (SearchStringMatch rightSearchWordMatch : rightSearchWordMatches)
                if (isMatchAdjacent(leftSearchWordMatch, rightSearchWordMatch))
                    ratingPoints = searchStringMatchRating.accumulatePoints(ratingPoints);

        return ratingPoints;
    }
}
