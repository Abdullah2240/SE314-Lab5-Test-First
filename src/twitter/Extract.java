/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            Instant now = Instant.now();
            return new Timespan(now, now);
        }
        Instant earliest = Instant.MAX;
        Instant latest = Instant.MIN;
        for (Tweet tweet : tweets) {
            Instant time = tweet.getTimestamp();
            if (time.compareTo(earliest) < 0) {
                earliest = time;
            }
            if (time.compareTo(latest) > 0) {
                latest = time;
            }
        }
        return new Timespan(earliest, latest);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentioned = new HashSet<>();
        for (Tweet tweet : tweets) {
            String text = tweet.getText();
            for (int at = text.indexOf('@'); at >= 0; at = text.indexOf('@', at + 1)) {
                if (at > 0 && isUsernameChar(text.charAt(at - 1))) {
                    continue;
                }
                int end = at + 1;
                while (end < text.length() && isUsernameChar(text.charAt(end))) {
                    end++;
                }
                if (end > at + 1) {
                    mentioned.add(text.substring(at + 1, end).toLowerCase());
                }
            }
        }
        return mentioned;
    }

    private static boolean isUsernameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
    }

}
