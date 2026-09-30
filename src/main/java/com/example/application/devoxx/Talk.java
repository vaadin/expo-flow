package com.example.application.devoxx;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One scheduled Devoxx talk, flattened from the CFP API to what the view shows and filters on.
 * Times are conference time (Europe/Brussels).
 */
public record Talk(String title, String speakers, String track, String format, String level, String room,
                   LocalDateTime start, LocalDateTime end, List<String> keywords, String description) {

    /** The text the topic filters search in. */
    String searchableText() {
        return String.join(" ", title, track, String.join(" ", keywords), description);
    }
}
