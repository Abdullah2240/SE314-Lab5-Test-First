/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy
     *
     * writtenBy(): partition by no, one, and multiple author matches and by
     * case differences between the requested and stored username. Multi-match
     * tests also check that input order is retained.
     *
     * inTimespan(): partition timestamps before, at start, strictly inside, at
     * end, and after a span. Tests include both endpoints and a no-match case.
     *
     * containing(): partition by no matching words, one/multiple requested
     * words, case differences, and words that differ only by punctuation.
     * Results are checked in input order, because all Filter methods require
     * that order.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    @Test
    public void testWrittenByCaseInsensitiveMultipleResultsInInputOrder() {
        Tweet laterAlyssa = new Tweet(3, "ALYSSA", "another tweet", d2);

        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet2, laterAlyssa, tweet1), "aLySsA");

        assertEquals(Arrays.asList(laterAlyssa, tweet1), writtenBy);
    }

    @Test
    public void testWrittenByNoMatches() {
        assertTrue(Filter.writtenBy(Arrays.asList(tweet1, tweet2), "carol").isEmpty());
    }
    
    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }

    @Test
    public void testInTimespanIncludesBothEndpointsAndExcludesOutside() {
        Instant before = Instant.parse("2016-02-17T09:59:59Z");
        Instant after = Instant.parse("2016-02-17T11:00:01Z");
        Tweet tooEarly = new Tweet(3, "carol", "before", before);
        Tweet tooLate = new Tweet(4, "dana", "after", after);

        List<Tweet> inTimespan = Filter.inTimespan(
                Arrays.asList(tooEarly, tweet2, tweet1, tooLate), new Timespan(d1, d2));

        assertEquals(Arrays.asList(tweet2, tweet1), inTimespan);
    }

    @Test
    public void testInTimespanNoMatches() {
        Timespan span = new Timespan(Instant.parse("2016-02-17T12:00:00Z"),
                Instant.parse("2016-02-17T13:00:00Z"));

        assertTrue(Filter.inTimespan(Arrays.asList(tweet1, tweet2), span).isEmpty());
    }
    
    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));
        
        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    @Test
    public void testContainingMatchesAnyWordCaseInsensitivelyInInputOrder() {
        Tweet first = new Tweet(3, "carol", "Coffee break", d1);
        Tweet second = new Tweet(4, "dana", "tea time", d2);
        Tweet third = new Tweet(5, "erin", "COFFEE and tea", d2);

        List<Tweet> containing = Filter.containing(Arrays.asList(second, first, third),
                Arrays.asList("coffee", "missing"));

        assertEquals(Arrays.asList(first, third), containing);
    }

    @Test
    public void testContainingTreatsPunctuationAsPartOfSpaceBoundedWord() {
        Tweet punctuation = new Tweet(6, "frank", "Hello, world!", d1);

        assertEquals(Arrays.asList(punctuation),
                Filter.containing(Arrays.asList(punctuation), Arrays.asList("hello,")));
        assertTrue(Filter.containing(Arrays.asList(punctuation), Arrays.asList("hello")).isEmpty());
    }

    @Test
    public void testContainingNoMatches() {
        assertTrue(Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("absent")).isEmpty());
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}
