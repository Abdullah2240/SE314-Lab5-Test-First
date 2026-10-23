/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {
    /*
     * Testing strategy for guessFollowsGraph(tweets):
     *   tweets: empty; one; several
     *   mentions: none; one; several; repeated with different capitalization
     *   author and mentioned user: different; same (self-mention)
     *   result: required mention edges, no self-edges, valid usernames, and
     *           case-insensitive uniqueness. An author with no outgoing edges
     *           may be omitted. We do not require an exact graph because the
     *           specification permits other evidence for following.
     *
     * Testing strategy for influencers(followsGraph):
     *   graph: empty; one user with no followers; users in keys and/or values
     *   follower counts: zero; one; several; tie
     *   usernames: distinct users; case variants of one user
     *   result: every distinct user appears once and counts descend. Ties may
     *           appear in either order because the specification does not say.
     */

    private static Tweet tweet(long id, String author, String text) {
        return new Tweet(id, author, text, Instant.parse("2026-10-23T09:00:00Z"));
    }

    private static String lower(String username) {
        return username.toLowerCase(Locale.ROOT);
    }

    private static Set<String> lowerNames(Iterable<String> names) {
        Set<String> result = new HashSet<>();
        for (String name : names) result.add(lower(name));
        return result;
    }

    private static Set<String> followedBy(Map<String, Set<String>> graph, String author) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(author)) {
                result.addAll(lowerNames(entry.getValue()));
            }
        }
        return result;
    }

    private static void assertValidGraph(Map<String, Set<String>> graph,
            String... allowedUsers) {
        Set<String> allowed = lowerNames(Arrays.asList(allowedUsers));
        assertEquals("one key per user, ignoring case",
                graph.size(), lowerNames(graph.keySet()).size());
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String author = lower(entry.getKey());
            assertTrue("key must come from the tweets", allowed.contains(author));
            assertEquals("followed users must be case-insensitively unique",
                    entry.getValue().size(), lowerNames(entry.getValue()).size());
            for (String followed : entry.getValue()) {
                assertTrue("followed user must come from the tweets",
                        allowed.contains(lower(followed)));
                assertFalse("users cannot follow themselves", author.equals(lower(followed)));
            }
        }
    }

    private static void assertAllUsersOnce(List<String> actual, String... expected) {
        Set<String> unique = lowerNames(actual);
        assertEquals("each user appears once", actual.size(), unique.size());
        assertEquals(lowerNames(Arrays.asList(expected)), unique);
    }

    private static int indexOfUser(List<String> users, String sought) {
        for (int i = 0; i < users.size(); ++i) {
            if (users.get(i).equalsIgnoreCase(sought)) return i;
        }
        fail("missing user " + sought);
        return -1;
    }

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false;
    }

    @Test
    public void testGuessFollowsGraphEmpty() {
        assertTrue(SocialNetwork.guessFollowsGraph(Collections.<Tweet>emptyList()).isEmpty());
    }

    @Test
    public void testGuessFollowsGraphWithoutMentions() {
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet(1, "Ernie", "Good morning")));
        assertValidGraph(graph, "Ernie");
    }

    @Test
    public void testGuessFollowsGraphSingleMention() {
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet(1, "Ernie", "Hello @Bert")));
        assertTrue(followedBy(graph, "Ernie").contains("bert"));
        assertValidGraph(graph, "Ernie", "Bert");
    }

    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet(1, "Ernie", "Hi @Bert and @Abby")));
        assertTrue(followedBy(graph, "Ernie").containsAll(Arrays.asList("bert", "abby")));
        assertValidGraph(graph, "Ernie", "Bert", "Abby");
    }

    @Test
    public void testGuessFollowsGraphMergesTweetsAndCase() {
        List<Tweet> tweets = new ArrayList<>(Arrays.asList(
                tweet(1, "Ernie", "@Bert"),
                tweet(2, "ERNIE", "@bert and @Abby")));
        List<Tweet> before = new ArrayList<>(tweets);
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue(followedBy(graph, "Ernie").containsAll(Arrays.asList("bert", "abby")));
        assertValidGraph(graph, "Ernie", "Bert", "Abby");
        assertEquals("input list must not change", before, tweets);
    }

    @Test
    public void testGuessFollowsGraphSelfMention() {
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet(1, "Ernie", "@ERNIE and @Bert")));
        assertTrue(followedBy(graph, "Ernie").contains("bert"));
        assertFalse(followedBy(graph, "Ernie").contains("ernie"));
        assertValidGraph(graph, "Ernie", "Bert");
    }

    @Test
    public void testInfluencersEmpty() {
        assertTrue(SocialNetwork.influencers(new HashMap<String, Set<String>>()).isEmpty());
    }

    @Test
    public void testInfluencersIncludesUserWithNoFollowers() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("Ernie", new HashSet<String>());
        assertAllUsersOnce(SocialNetwork.influencers(graph), "Ernie");
    }

    @Test
    public void testInfluencersIncludesFollowedUserWithoutKey() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("Ernie", new HashSet<>(Arrays.asList("Bert")));
        List<String> result = SocialNetwork.influencers(graph);
        assertAllUsersOnce(result, "Ernie", "Bert");
        assertTrue(indexOfUser(result, "Bert") < indexOfUser(result, "Ernie"));
    }

    @Test
    public void testInfluencersRanksByFollowerCount() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("Abby", new HashSet<>(Arrays.asList("Bert")));
        graph.put("Ernie", new HashSet<>(Arrays.asList("Bert", "Abby")));
        graph.put("Zoe", new HashSet<>(Arrays.asList("Bert")));
        List<String> result = SocialNetwork.influencers(graph);
        assertAllUsersOnce(result, "Abby", "Ernie", "Zoe", "Bert");
        assertTrue(indexOfUser(result, "Bert") < indexOfUser(result, "Abby"));
        assertTrue(indexOfUser(result, "Abby") < indexOfUser(result, "Ernie"));
        assertTrue(indexOfUser(result, "Abby") < indexOfUser(result, "Zoe"));
    }

    @Test
    public void testInfluencersAllowsEitherOrderForTie() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("Abby", new HashSet<>(Arrays.asList("Bert", "Ernie")));
        List<String> result = SocialNetwork.influencers(graph);
        assertAllUsersOnce(result, "Abby", "Bert", "Ernie");
        assertTrue(indexOfUser(result, "Bert") < indexOfUser(result, "Abby"));
        assertTrue(indexOfUser(result, "Ernie") < indexOfUser(result, "Abby"));
    }

    @Test
    public void testInfluencersTreatsCaseVariantsAsOneUser() {
        Map<String, Set<String>> graph = new HashMap<>();
        graph.put("Abby", new HashSet<>(Arrays.asList("Bert")));
        graph.put("Ernie", new HashSet<>(Arrays.asList("bert")));
        List<String> result = SocialNetwork.influencers(graph);
        assertAllUsersOnce(result, "Abby", "Ernie", "Bert");
        assertTrue(indexOfUser(result, "Bert") < indexOfUser(result, "Abby"));
        assertTrue(indexOfUser(result, "Bert") < indexOfUser(result, "Ernie"));
    }
}
