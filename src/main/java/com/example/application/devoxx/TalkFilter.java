package com.example.application.devoxx;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * What the talk search form asks for. Every criterion is optional: a blank text, an empty set or a
 * {@code null} time means "no constraint".
 *
 * @param topics         comma-separated words; a talk matches if it contains any of them
 * @param excludedTopics comma-separated words; a talk is left out if it contains any of them
 */
public record TalkFilter(String topics, String excludedTopics, String speaker,
                         Set<String> tracks, Set<String> formats, Set<String> levels,
                         LocalDateTime startsAfter, LocalDateTime startsBefore) {

    public static TalkFilter empty() {
        return new TalkFilter("", "", "", Set.of(), Set.of(), Set.of(), null, null);
    }

    public boolean matches(Talk talk) {
        return (topics.isBlank() || containsAnyWord(talk.searchableText(), topics))
                && (excludedTopics.isBlank() || !containsAnyWord(talk.searchableText(), excludedTopics))
                && (speaker.isBlank() || talk.speakers().toLowerCase().contains(speaker.strip().toLowerCase()))
                && (tracks.isEmpty() || tracks.contains(talk.track()))
                && (formats.isEmpty() || formats.contains(talk.format()))
                && (levels.isEmpty() || levels.contains(talk.level()))
                && (startsAfter == null || !talk.start().isBefore(startsAfter))
                && (startsBefore == null || !talk.start().isAfter(startsBefore));
    }

    /**
     * Whether the text contains one of the comma-separated words as a whole word, ignoring case —
     * so "AI" matches "AI agents" but not "maintain".
     */
    private static boolean containsAnyWord(String text, String commaSeparatedWords) {
        return Arrays.stream(commaSeparatedWords.split(","))
                .map(String::strip)
                .filter(word -> !word.isEmpty())
                .map(word -> Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(word) + "(?![\\p{L}\\p{N}])",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                .anyMatch(pattern -> pattern.matcher(text).find());
    }
}
