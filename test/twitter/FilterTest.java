/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /* Testing strategy
     *
     * writtenBy(tweets, username):
     *   tweets by that user: none, one, several
     *   username case: same as author, different from author
     *
     * inTimespan(tweets, timespan):
     *   tweets inside: none, some, all
     *   timestamp equal to start or end (endpoints are included)
     *
     * containing(tweets, words):
     *   words.size(): 0, 1, more than 1
     *   case of the word: same, different from the tweet
     *   word appears only inside a longer word (no match)
     *
     * Every result must keep the order of the input list.
     */
    
    private static final Instant t1 = Instant.parse("2016-03-01T08:15:00Z");
    private static final Instant t2 = Instant.parse("2016-03-01T09:40:00Z");
    private static final Instant t3 = Instant.parse("2016-03-02T18:05:00Z");
    
    private static final Tweet first = new Tweet(10, "ben_b", "Lab is due on Friday", t1);
    private static final Tweet second = new Tweet(11, "louis", "the lab is long but fine", t2);
    private static final Tweet third = new Tweet(12, "Ben_B", "labs and more labs #tired", t3);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testWrittenByNobody() {
        List<Tweet> result = Filter.writtenBy(Arrays.asList(first, second, third), "eva");
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testWrittenByOne() {
        List<Tweet> result = Filter.writtenBy(Arrays.asList(first, second, third), "louis");
        assertEquals(Arrays.asList(second), result);
    }
    
    @Test
    public void testWrittenBySeveralIgnoringCase() {
        List<Tweet> result = Filter.writtenBy(Arrays.asList(first, second, third), "BEN_B");
        assertEquals(Arrays.asList(first, third), result);
    }
    
    @Test
    public void testInTimespanAll() {
        Timespan span = new Timespan(Instant.parse("2016-03-01T00:00:00Z"), Instant.parse("2016-03-03T00:00:00Z"));
        List<Tweet> result = Filter.inTimespan(Arrays.asList(third, first, second), span);
        assertEquals(Arrays.asList(third, first, second), result);
    }
    
    @Test
    public void testInTimespanEndpointsIncluded() {
        List<Tweet> result = Filter.inTimespan(Arrays.asList(first, second, third), new Timespan(t1, t2));
        assertEquals(Arrays.asList(first, second), result);
    }
    
    @Test
    public void testInTimespanNone() {
        Timespan span = new Timespan(Instant.parse("2016-03-01T10:00:00Z"), Instant.parse("2016-03-01T12:00:00Z"));
        List<Tweet> result = Filter.inTimespan(Arrays.asList(first, second, third), span);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testContainingNoWords() {
        List<Tweet> result = Filter.containing(Arrays.asList(first, second), new ArrayList<String>());
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void testContainingDifferentCase() {
        List<Tweet> result = Filter.containing(Arrays.asList(first, second, third), Arrays.asList("LAB"));
        assertEquals(Arrays.asList(first, second), result);
    }
    
    @Test
    public void testContainingSeveralWords() {
        List<Tweet> result = Filter.containing(Arrays.asList(first, second, third), Arrays.asList("friday", "#tired"));
        assertEquals(Arrays.asList(first, third), result);
    }
    
    @Test
    public void testContainingPartOfWordOnly() {
        List<Tweet> result = Filter.containing(Arrays.asList(first, second, third), Arrays.asList("fin", "tire"));
        assertTrue(result.isEmpty());
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
