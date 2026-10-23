/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy
     *
     * getTimespan(): partition tweet lists by size (one versus several), and
     * for several tweets by timestamp order (already ordered versus unordered)
     * and by whether timestamp values repeat. The specification requires the
     * minimum-length interval, so each test checks the earliest and latest
     * values exactly.
     *
     * getMentionedUsers(): partition text by no mention versus mentions; valid
     * mention at text boundaries versus next to punctuation; repeated users
     * with different case; and an @ sequence invalidated by a username
     * character immediately before or after it. Returned spelling is not
     * specified, so tests compare normalized usernames.
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
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanOneTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2));

        assertEquals("single tweet start", d2, timespan.getStart());
        assertEquals("single tweet end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanUnorderedTweetsWithRepeatedTimestamp() {
        Instant d0 = Instant.parse("2016-02-17T09:00:00Z");
        Tweet earliest = new Tweet(3, "carol", "early", d0);
        Tweet repeatedLatest = new Tweet(4, "dana", "late too", d2);

        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, earliest, repeatedLatest));

        assertEquals("earliest timestamp", d0, timespan.getStart());
        assertEquals("latest timestamp", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersDistinctCaseInsensitiveAndPunctuationBounded() {
        Tweet mentions = new Tweet(5, "erin", "@Alice, meet @bob-2 and @ALICE!", d1);

        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(mentions));

        assertEquals("two logical users", 2, mentionedUsers.size());
        assertEquals(new HashSet<String>(Arrays.asList("alice", "bob-2")), lowercase(mentionedUsers));
    }

    @Test
    public void testGetMentionedUsersRejectsEmailStyleSequence() {
        Tweet invalid = new Tweet(6, "frank", "mail a@alice.com and @bob_2x, but @carol.", d2);

        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(invalid));

        assertEquals(new HashSet<String>(Arrays.asList("bob_2x", "carol")), lowercase(mentionedUsers));
    }

    private static Set<String> lowercase(Set<String> usernames) {
        Set<String> normalized = new HashSet<String>();
        for (String username : usernames) {
            normalized.add(username.toLowerCase());
        }
        return normalized;
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
