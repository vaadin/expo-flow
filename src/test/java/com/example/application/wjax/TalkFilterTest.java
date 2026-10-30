package com.example.application.wjax;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalkFilterTest {

    private final Talk talk = new Talk("Maintaining Java apps", "Duke",
            List.of("Java & JVM Languages", "Java Backend Engineering"), "Session", "English", "Ballsaal",
            LocalDateTime.of(2026, 11, 4, 10, 0), LocalDateTime.of(2026, 11, 4, 11, 0),
            "How to keep a big JVM codebase healthy.");

    @Test
    void topicsMatchWholeWordsOnly() {
        assertTrue(topics("java").matches(talk));
        assertTrue(topics("kotlin, JVM").matches(talk)); // any of them, ignoring case
        assertFalse(topics("AI").matches(talk)); // not inside "Maintaining"
    }

    @Test
    void excludedTopicsLeaveTalksOut() {
        assertFalse(new TalkFilter("", "codebase", "", Set.of(), Set.of(), Set.of(), null, null).matches(talk));
        assertTrue(new TalkFilter("", "AI, LLM", "", Set.of(), Set.of(), Set.of(), null, null).matches(talk));
    }

    @Test
    void matchesAnyOfTheTalksTracks() {
        assertTrue(tracks("Java Backend Engineering", "Frontend & Web Applications").matches(talk));
        assertFalse(tracks("Frontend & Web Applications").matches(talk));
    }

    @Test
    void startTimeBoundsAreInclusive() {
        var start = talk.start();
        assertTrue(new TalkFilter("", "", "", Set.of(), Set.of(), Set.of(), start, start).matches(talk));
        assertFalse(new TalkFilter("", "", "", Set.of(), Set.of(), Set.of(), start.plusMinutes(1), null).matches(talk));
    }

    private static TalkFilter topics(String topics) {
        return new TalkFilter(topics, "", "", Set.of(), Set.of(), Set.of(), null, null);
    }

    private static TalkFilter tracks(String... tracks) {
        return new TalkFilter("", "", "", Set.of(tracks), Set.of(), Set.of(), null, null);
    }
}
