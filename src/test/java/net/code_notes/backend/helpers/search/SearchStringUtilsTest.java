package net.code_notes.backend.helpers.search;

import static net.code_notes.backend.helpers.Utils.SEARCH_ADJACENT_MATCH_RATING_POINTS;
import static net.code_notes.backend.helpers.Utils.SEARCH_APPROXIMATE_RATING_POINTS;
import static net.code_notes.backend.helpers.Utils.SEARCH_EXACT_MATCH_RATING_POINTS;
import static net.code_notes.backend.helpers.Utils.SEARCH_WORD_MIN_LENGTH_FOR_CONTAINS;
import static net.code_notes.backend.helpers.Utils.isBlank;
import static net.code_notes.backend.helpers.Utils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
public class SearchStringUtilsTest {

    @Test
    void isExactMatch_shouldHandleNullAndBlankAndEmptyArgsLikeNormalString() {
        String searchWord = null;
        String compareWord = null;

        // both null
        assertNull(searchWord);
        assertNull(compareWord);
        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));

        // one null
        searchWord = "not-null";
        assertNotNull(searchWord);
        assertNull(compareWord);
        assertFalse(SearchStringUtils.isExactMatch(searchWord, compareWord));

        compareWord = "not-null";
        searchWord = null;
        assertNull(searchWord);
        assertNotNull(compareWord);
        assertFalse(SearchStringUtils.isExactMatch(searchWord, compareWord));

        // blank
        searchWord = " ";
        compareWord = " ";
        assertTrue(isBlank(searchWord));
        assertTrue(isBlank(compareWord));
        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));

        // empty
        searchWord = "";
        compareWord = "";
        assertTrue(isEmpty(searchWord));
        assertTrue(isEmpty(compareWord));
        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));
    }

    @Test
    void isExactMatch_shouldWordLikeEqualsIgnoreCase() {
        String searchWord = "noteTitle";
        String compareWord = "noteTitle";

        // same case
        assertEquals(searchWord, compareWord);
        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));

        // ignore case
        searchWord = "notetitle";
        assertNotEquals(searchWord, compareWord);
        assertTrue(searchWord.equalsIgnoreCase(compareWord));
        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));

        compareWord = searchWord;
        searchWord = "noteTitle";
        assertNotEquals(searchWord, compareWord);
        assertTrue(searchWord.equalsIgnoreCase(compareWord));
        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));

        // not equal
        compareWord = "noTheTitle";
        assertNotEquals(searchWord, compareWord);
        assertFalse(SearchStringUtils.isExactMatch(searchWord, compareWord));
    }

    @Test
    void isApproximateMatch_shouldHandleNullAndBlankAndEmptyArgsCorrectly() {
        String searchWord = null;
        String compareWord = null;

        // both null
        assertNull(searchWord);
        assertNull(compareWord);
        assertFalse(SearchStringUtils.isApproximateMatch(searchWord, compareWord));

        // one null
        searchWord = "not-null";
        assertNotNull(searchWord);
        assertNull(compareWord);
        assertFalse(SearchStringUtils.isApproximateMatch(searchWord, compareWord));

        compareWord = "not-null";
        searchWord = null;
        assertNull(searchWord);
        assertNotNull(compareWord);
        assertFalse(SearchStringUtils.isApproximateMatch(searchWord, compareWord));

        // blank
        searchWord = " ";
        compareWord = " ";
        assertTrue(isBlank(searchWord));
        assertTrue(isBlank(compareWord));
        assertTrue(SearchStringUtils.isApproximateMatch(searchWord, compareWord));

        // empty
        searchWord = "";
        compareWord = "";
        assertTrue(isEmpty(searchWord));
        assertTrue(isEmpty(compareWord));
        assertTrue(SearchStringUtils.isApproximateMatch(searchWord, compareWord));
    }

    @Test
    void isApproximateMatch_shouldHandleLongerSearchWordWithContainsIgnoreCase() {
        String compareWord = "abcdefg";
        String searchWord = "bcdef";

        // same case
        assertTrue(searchWord.length() >= SEARCH_WORD_MIN_LENGTH_FOR_CONTAINS);
        assertTrue(Strings.CI.contains(compareWord, searchWord));
        assertTrue(SearchStringUtils.isApproximateMatch(searchWord, compareWord));

        // different case
        searchWord = "Bcdef";
        assertTrue(searchWord.length() >= SEARCH_WORD_MIN_LENGTH_FOR_CONTAINS);
        assertTrue(SearchStringUtils.isApproximateMatch(searchWord, compareWord));
        searchWord = "bcdef";

        // should not work the other way round
        compareWord = "bcd";
        assertTrue(searchWord.length() >= SEARCH_WORD_MIN_LENGTH_FOR_CONTAINS);
        assertTrue(searchWord.contains(compareWord));
        assertFalse(SearchStringUtils.isApproximateMatch(searchWord, compareWord));
        compareWord = "abcdefg";

        // does not contain
        compareWord = "doesNotContainSearchWord";
        assertTrue(searchWord.length() >= SEARCH_WORD_MIN_LENGTH_FOR_CONTAINS);
        assertFalse(compareWord.contains(searchWord));
        assertFalse(SearchStringUtils.isApproximateMatch(searchWord, compareWord));
    }

    @Test
    void isMatchAdjacent_shouldBeFalseIfAnyArgIsNull() {
        SearchStringMatch leftSearchStringMatch = null;
        SearchStringMatch rightSearchStringMatch = null;

        assertNull(leftSearchStringMatch);
        assertNull(rightSearchStringMatch);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        leftSearchStringMatch = new SearchStringMatch();
        assertNotNull(leftSearchStringMatch);
        assertNull(rightSearchStringMatch);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        rightSearchStringMatch = new SearchStringMatch();
        leftSearchStringMatch = null;
        assertNotNull(rightSearchStringMatch);
        assertNull(leftSearchStringMatch);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));
    }

    @Test
    void isMatchAdjacent_shouldBeFalseIfSameSearchWord() {
        SearchStringMatch leftSearchStringMatch = new SearchStringMatch(0, 5, 1, 0, new ApproximateSearchStringMatchRating());
        SearchStringMatch rightSearchStringMatch = new SearchStringMatch(0, 5, 1, 0, new ApproximateSearchStringMatchRating());

        assertEquals(leftSearchStringMatch.getSearchWordIndex(), rightSearchStringMatch.getSearchWordIndex());
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));
    }

    @Test
    void isMatchAdjacent_shouldBeFalseIfSearchWordsNotAdjacentOrOutOfOrder() {
        SearchStringMatch leftSearchStringMatch = new SearchStringMatch(0, 5, 1, 0, new ApproximateSearchStringMatchRating());
        SearchStringMatch rightSearchStringMatch = new SearchStringMatch(2, 5, 1, 0, new ApproximateSearchStringMatchRating());

        // not adjacent
        assertFalse(rightSearchStringMatch.getSearchWordIndex() - leftSearchStringMatch.getSearchWordIndex() == 1);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        // left search word is actually on the right
        leftSearchStringMatch.setSearchWordIndex(2);
        rightSearchStringMatch.setSearchWordIndex(1);
        assertFalse(rightSearchStringMatch.getSearchWordIndex() - leftSearchStringMatch.getSearchWordIndex() == 1);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));
    }

    @Test
    void isMatchAdjacent_matchesSameCompareWord() {
        SearchStringMatch leftSearchStringMatch = new SearchStringMatch(0, 5, 1, 0, new ApproximateSearchStringMatchRating());
        SearchStringMatch rightSearchStringMatch = new SearchStringMatch(1, 7, 1, leftSearchStringMatch.getCompareWordSubstringStartIndex() + leftSearchStringMatch.getSearchWordLength(), new ApproximateSearchStringMatchRating());

        // no overlap, left is on the left
        assertEquals(leftSearchStringMatch.getCompareWordIndex(), rightSearchStringMatch.getCompareWordIndex());
        assertTrue(rightSearchStringMatch.getCompareWordSubstringStartIndex() > leftSearchStringMatch.getCompareWordSubstringStartIndex() + leftSearchStringMatch.getSearchWordLength() - 1);
        assertTrue(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        // overlap, left is further on the left (by 1 char)
        rightSearchStringMatch.setCompareWordSubstringStartIndex(leftSearchStringMatch.getCompareWordSubstringStartIndex() + 1);
        assertEquals(leftSearchStringMatch.getCompareWordIndex(), rightSearchStringMatch.getCompareWordIndex());
        assertFalse(rightSearchStringMatch.getCompareWordSubstringStartIndex() > leftSearchStringMatch.getCompareWordSubstringStartIndex() + leftSearchStringMatch.getSearchWordLength() - 1);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        // no overlap, left is on the right
        rightSearchStringMatch = new SearchStringMatch(0, 5, 1, 0, new ApproximateSearchStringMatchRating());
        leftSearchStringMatch =  new SearchStringMatch(1, 7, 1, rightSearchStringMatch.getCompareWordSubstringStartIndex() + rightSearchStringMatch.getSearchWordLength(), new ApproximateSearchStringMatchRating());
        assertEquals(leftSearchStringMatch.getCompareWordIndex(), rightSearchStringMatch.getCompareWordIndex());
        assertFalse(rightSearchStringMatch.getCompareWordSubstringStartIndex() > leftSearchStringMatch.getCompareWordSubstringStartIndex() + leftSearchStringMatch.getSearchWordLength() - 1);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));
    }

    @Test
    void isMatchAdjacent_matchesDifferentCompareWord() {
        SearchStringMatch leftSearchStringMatch = new SearchStringMatch(0, 5, 1, 0, new ApproximateSearchStringMatchRating());
        SearchStringMatch rightSearchStringMatch = new SearchStringMatch(1, 7, 2, 0, new ApproximateSearchStringMatchRating());

        // adjacent compareword in correct order, right compare word starts at 0
        assertTrue(rightSearchStringMatch.getCompareWordIndex() - leftSearchStringMatch.getCompareWordIndex() == 1);
        assertEquals(rightSearchStringMatch.getCompareWordSubstringStartIndex(), 0);
        assertTrue(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        // adjacent compareword in correct order, right compare word starts at i > 0
        rightSearchStringMatch.setCompareWordSubstringStartIndex(3);
        assertTrue(rightSearchStringMatch.getCompareWordIndex() - leftSearchStringMatch.getCompareWordIndex() == 1);
        assertTrue(rightSearchStringMatch.getCompareWordSubstringStartIndex() > 0);
        assertTrue(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        // compareword not adjacent in correct order
        rightSearchStringMatch.setCompareWordIndex(rightSearchStringMatch.getCompareWordIndex() + 1);
        assertTrue(rightSearchStringMatch.getCompareWordIndex() - leftSearchStringMatch.getCompareWordIndex() > 1);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));

        // compare word in reverse order
        rightSearchStringMatch.setCompareWordIndex(leftSearchStringMatch.getCompareWordIndex() - 1);
        assertTrue(rightSearchStringMatch.getCompareWordIndex() - leftSearchStringMatch.getCompareWordIndex() < 0);
        assertFalse(SearchStringUtils.isMatchAdjacent(leftSearchStringMatch, rightSearchStringMatch));
    }

    @Test
    void matchWords_shouldHandleNull() {
        String leftSearchWord = null;
        String rightSearchWord = null;

        assertNull(leftSearchWord);
        assertNull(rightSearchWord);
        SearchStringMatch searchStringMatch = SearchStringUtils.matchWords(leftSearchWord, rightSearchWord);
        assertEquals(0, searchStringMatch.getCompareWordSubstringStartIndex());
        assertEquals(SEARCH_EXACT_MATCH_RATING_POINTS, searchStringMatch.getRating().getPoints());

        leftSearchWord = "";
        assertNotNull(leftSearchWord);
        assertNull(rightSearchWord);
        searchStringMatch = SearchStringUtils.matchWords(leftSearchWord, rightSearchWord);
        assertEquals(-1, searchStringMatch.getCompareWordSubstringStartIndex());
        assertEquals(0, searchStringMatch.getRating().getPoints());

        leftSearchWord = null;
        rightSearchWord = "";
        assertNull(leftSearchWord);
        assertNotNull(rightSearchWord);
        searchStringMatch = SearchStringUtils.matchWords(leftSearchWord, rightSearchWord);
        assertEquals(-1, searchStringMatch.getCompareWordSubstringStartIndex());
        assertEquals(0, searchStringMatch.getRating().getPoints());
    }

    @Test
    void matchWords_assertExactMatchRatingPoints() {
        String searchWord = "input";
        String compareWord = "input";

        assertTrue(SearchStringUtils.isExactMatch(searchWord, compareWord));
        SearchStringMatch searchStringMatch = SearchStringUtils.matchWords(searchWord, compareWord);
        assertInstanceOf(ExactSearchStringMatchRating.class, searchStringMatch.getRating());
        assertEquals(SEARCH_EXACT_MATCH_RATING_POINTS, searchStringMatch.getRating().getPoints());
        assertEquals(0, searchStringMatch.getCompareWordSubstringStartIndex());
    }
    
    @Test
    void matchWords_assertApproximateMatchRatingPoints() {
        String searchWord = "input";
        String compareWord = "searchInput";
        int compareWordSubstringStartIndex = Strings.CI.indexOf(compareWord, searchWord);

        // contains
        assertTrue(Strings.CI.contains(compareWord, searchWord));
        assertTrue(SearchStringUtils.isApproximateMatch(searchWord, compareWord));
        SearchStringMatch searchStringMatch = SearchStringUtils.matchWords(searchWord, compareWord);
        assertInstanceOf(ApproximateSearchStringMatchRating.class, searchStringMatch.getRating());
        assertEquals(SEARCH_APPROXIMATE_RATING_POINTS, searchStringMatch.getRating().getPoints());
        assertEquals(compareWordSubstringStartIndex, searchStringMatch.getCompareWordSubstringStartIndex());
    }

    @Test
    void matchWords_noMatchShouldReturnNegativeIndexAndRatingOf0() {
        String searchWord = "input";
        String compareWord = "notAMatch";

        assertNotNull(searchWord);
        assertNotNull(compareWord);
        SearchStringMatch searchStringMatch = SearchStringUtils.matchWords(searchWord, compareWord);
        assertEquals(-1, searchStringMatch.getCompareWordSubstringStartIndex());
        assertEquals(0, searchStringMatch.getRating().getPoints());
    }

    @Test
    void matchPhrases_shouldHandleNull() {
        String searchPhrase = null;
        String comparePhrase = null;

        assertNull(searchPhrase);
        assertNull(comparePhrase);
        assertEquals(SEARCH_EXACT_MATCH_RATING_POINTS, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));

        searchPhrase = "";
        assertNotNull(searchPhrase);
        assertNull(comparePhrase);
        assertEquals(0, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));

        searchPhrase = null;
        comparePhrase = "";
        assertNull(searchPhrase);
        assertNotNull(comparePhrase);
        assertEquals(0, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));
    }

    @Test
    void matchPhrases_shouldHandleBlank() {
        String searchPhrase = "";
        String comparePhrase = "";

        assertTrue(searchPhrase.isEmpty());
        assertTrue(comparePhrase.isEmpty());
        assertEquals(SEARCH_EXACT_MATCH_RATING_POINTS, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));

        comparePhrase = " ";
        assertTrue(comparePhrase.isBlank());
        assertFalse(comparePhrase.isEmpty());
        assertTrue(searchPhrase.isEmpty());
        assertEquals(SEARCH_APPROXIMATE_RATING_POINTS, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));
        
        searchPhrase = "   ";
        comparePhrase = "";
        assertTrue(searchPhrase.isBlank());
        assertTrue(searchPhrase.length() > comparePhrase.length());
        assertFalse(searchPhrase.isEmpty());
        assertTrue(comparePhrase.isEmpty());
        assertEquals(0, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));

        comparePhrase = " ";
        searchPhrase = " ";
        assertTrue(searchPhrase.isBlank());
        assertTrue(comparePhrase.isBlank());
        assertFalse(searchPhrase.isEmpty());
        assertFalse(comparePhrase.isEmpty());
        assertEquals(SEARCH_EXACT_MATCH_RATING_POINTS, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));

        // should not use blank chars as match if there're other non-blank chars
        searchPhrase = "test test";
        comparePhrase = "noMatch noMatch";
        assertFalse(searchPhrase.isBlank());
        assertFalse(comparePhrase.isBlank());
        assertEquals(0, SearchStringUtils.matchPhrases(searchPhrase, comparePhrase));
    }

    @Test
    void matchPhrases_onlyReturnMatchWithHighestRating() {
        String searchWord = "input1 input3 input2";
        // no adjacent matches in this test
        String compareWord = "input1AndSomeMore input1AndSomeMore containsInput2 containsInput2 containsInput2 input3 containsInput3";

        int numApproximateMatches = 2;
        assertEquals((SEARCH_APPROXIMATE_RATING_POINTS * numApproximateMatches) + SEARCH_EXACT_MATCH_RATING_POINTS, SearchStringUtils.matchPhrases(searchWord, compareWord));

        compareWord = "input1AndSomeMore input1AndSomeMore containsInput2 containsInput2 containsInput2 containsInput3";
        numApproximateMatches++;
        assertEquals((SEARCH_APPROXIMATE_RATING_POINTS * numApproximateMatches), SearchStringUtils.matchPhrases(searchWord, compareWord));
    }
    
    @Test
    void matchPhrases_considerAllAdjacentMatches() {
        String searchWord = "input1 input3 input2";
        String compareWord = "input1AndSomeMore containsInput2 input3";

        // no adjacent match
        int numApproximateMatches = 2;
        assertEquals((SEARCH_APPROXIMATE_RATING_POINTS * numApproximateMatches) + SEARCH_EXACT_MATCH_RATING_POINTS, SearchStringUtils.matchPhrases(searchWord, compareWord));

        // 1 adjacent match with approx match
        searchWord = "input1 input2";
        assertEquals((SEARCH_APPROXIMATE_RATING_POINTS * numApproximateMatches) + SEARCH_ADJACENT_MATCH_RATING_POINTS, SearchStringUtils.matchPhrases(searchWord, compareWord));

        // 2 adjacent matches with exact match
        searchWord = "input1 input2 input3";
        assertEquals(SEARCH_APPROXIMATE_RATING_POINTS + (SEARCH_APPROXIMATE_RATING_POINTS + SEARCH_ADJACENT_MATCH_RATING_POINTS) + (SEARCH_EXACT_MATCH_RATING_POINTS + SEARCH_ADJACENT_MATCH_RATING_POINTS), SearchStringUtils.matchPhrases(searchWord, compareWord));

        // 3 adjacent matches, consider only on match each
        int numAdjacentMatches = 3;
        compareWord = "input1AndSomeMore containsInput2 input1AndSomeMore containsInput2 input3 containsInput3";
        assertEquals((SEARCH_APPROXIMATE_RATING_POINTS * numApproximateMatches) + SEARCH_EXACT_MATCH_RATING_POINTS + (SEARCH_ADJACENT_MATCH_RATING_POINTS * numAdjacentMatches), SearchStringUtils.matchPhrases(searchWord, compareWord));
    }
}
