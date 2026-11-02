package com.example.application.wjax;

import java.util.Arrays;

/**
 * The W-JAX 2026 tracks, in alphabetical order of their names. The name is how the conference
 * planner's XML export spells the track; the short name fits on a badge in the talk grid.
 */
public enum Track {

    AI_DRIVEN_ENGINEERING("AI-Driven Software Engineering", "AI Engineering"),
    AGILE("Agile, Flow & Engineering Culture", "Agile"),
    CLOUD_NATIVE("Cloud Native & Kubernetes", "Cloud"),
    DEVOPS("DevOps, CI/CD & Platform Engineering", "DevOps"),
    ENTERPRISE_AI("Enterprise AI & Agentic Systems", "Enterprise AI"),
    FRONTEND("Frontend & Web Applications", "Frontend"),
    JAVA("Java & JVM Languages", "Java"),
    JAVA_BACKEND("Java Backend Engineering", "Backend"),
    PERFORMANCE_SECURITY("Performance & Security", "Security"),
    ARCHITECTURE("Software Architektur & AI Integration", "Architektur"),
    TOGETHER("W-JAX Together", "Together");

    private final String displayName;
    private final String shortName;

    Track(String displayName, String shortName) {
        this.displayName = displayName;
        this.shortName = shortName;
    }

    /** The full name, as in the XML export. The search form and the AI use it. */
    public String displayName() {
        return displayName;
    }

    /** A one- or two-word name for the badge in the grid. */
    public String shortName() {
        return shortName;
    }

    /** The track with this name in the XML export. A track not listed here fails the startup. */
    public static Track fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(track -> track.displayName.equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown W-JAX track: " + displayName));
    }
}
