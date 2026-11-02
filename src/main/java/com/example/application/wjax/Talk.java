package com.example.application.wjax;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One scheduled W-JAX talk, flattened from the XML export to what the view shows and filters on.
 * Times are conference time (Europe/Berlin). Most talks have several tracks, the primary one first.
 */
public record Talk(String title, String speakers, List<Track> tracks, String format, String language,
                   String room, LocalDateTime start, LocalDateTime end, String description) {

    /** The tracks for display. Track names contain commas, so they are separated by a dot. */
    public String trackNames() {
        return tracks.stream().map(Track::displayName).collect(Collectors.joining(" · "));
    }

    /** The text the topic filters search in. */
    String searchableText() {
        return String.join(" ", title, tracks.stream().map(Track::displayName).collect(Collectors.joining(" ")),
                description);
    }
}
