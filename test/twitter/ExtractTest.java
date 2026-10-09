/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /* Testing strategy
     *
     * getTimespan(tweets):
     *   tweets.size(): 1, 2, more than 2
     *   input order: oldest first, newest first, mixed
     *   two tweets with the same timestamp: yes, no
     *
     * getMentionedUsers(tweets):
     *   mentions in the text: none, one, several
     *   username case: same, different (same user is counted once)
     *   character before "@": start of text, space, username character (not a mention)
     *   character after the username: end of text, punctuation, space
     *
     * The spec allows any case in the result, so usernames are lowercased before comparing.
     */
    
    private static final Instant t1 = Instant.parse("2016-03-01T08:15:00Z");
    private static final Instant t2 = Instant.parse("2016-03-01T09:40:00Z");
    private static final Instant t3 = Instant.parse("2016-03-02T18:05:00Z");
    
    private static final Tweet first = new Tweet(10, "ben_b", "@louis lab is due on friday", t1);
    private static final Tweet second = new Tweet(11, "louis", "ok, working on it with @Ben_B and @cy-hacker.", t2);
    private static final Tweet third = new Tweet(12, "cy-hacker", "anyone free tonight? write to cy@mit.edu", t3);
    private static final Tweet fourth = new Tweet(13, "eva", "no plans today", t2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testTimespanSingleTweet() {
        Timespan span = Extract.getTimespan(Arrays.asList(third));
        assertEquals(t3, span.getStart());
        assertEquals(t3, span.getEnd());
    }
    
    @Test
    public void testTimespanNewestFirst() {
        Timespan span = Extract.getTimespan(Arrays.asList(second, first));
        assertEquals(t1, span.getStart());
        assertEquals(t2, span.getEnd());
    }
    
    @Test
    public void testTimespanMixedOrderSameTime() {
        Timespan span = Extract.getTimespan(Arrays.asList(second, third, first, fourth));
        assertEquals(t1, span.getStart());
        assertEquals(t3, span.getEnd());
    }
    
    @Test
    public void testMentionedUsersNone() {
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(fourth));
        assertTrue(users.isEmpty());
    }
    
    @Test
    public void testMentionedUsersOneAtStart() {
        Set<String> users = toLower(Extract.getMentionedUsers(Arrays.asList(first)));
        assertEquals(Collections.singleton("louis"), users);
    }
    
    @Test
    public void testMentionedUsersSeveralBeforePunctuation() {
        Set<String> users = toLower(Extract.getMentionedUsers(Arrays.asList(second)));
        assertEquals(new HashSet<>(Arrays.asList("ben_b", "cy-hacker")), users);
    }
    
    @Test
    public void testMentionedUsersEmailIgnored() {
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(third));
        assertTrue(users.isEmpty());
    }
    
    @Test
    public void testMentionedUsersSameUserDifferentCase() {
        Tweet upper = new Tweet(14, "eva", "@LOUIS see you", t3);
        Set<String> users = Extract.getMentionedUsers(Arrays.asList(first, upper));
        assertEquals(1, users.size());
        assertEquals(Collections.singleton("louis"), toLower(users));
    }
    
    private static Set<String> toLower(Set<String> names) {
        Set<String> lower = new HashSet<>();
        for (String name : names) {
            lower.add(name.toLowerCase());
        }
        return lower;
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}
