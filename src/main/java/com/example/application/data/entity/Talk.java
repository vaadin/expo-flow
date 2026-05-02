package com.example.application.data.entity;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Talk {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NonNull
    private String title;

    @NonNull
    private String speaker;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "talk_tracks", joinColumns = @JoinColumn(name = "talk_id"))
    @Column(name = "track")
    private List<Track> tracks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Format format;

    private LocalDate startDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String room;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NonNull String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public @NonNull String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(@NonNull String speaker) {
        this.speaker = speaker;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "Talk{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", speaker='" + speaker + '\'' +
                ", format=" + format +
                ", startDate=" + startDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", room='" + room + '\'' +
                '}';
    }

    public enum Format {
        KEYNOTE, SESSION, WORKSHOP, PANEL, LAB, SHORTTALK
    }

    public enum Track {
        AGILE("Agile, People & Culture (JAX)"),
        AGILE_FLOW("Agile Flow Day - Modern Productivity (JAX)"),
        ARCH("Architecture & Design (JAX)"),
        CLOUD("Clouds, Kubernetes & Serverless (JAX)"),
        CORE_JAVA("Core Java & Languages (JAX)"),
        DATA_ML("Data & Machine Learning (JAX)"),
        DEVOPS("DevOps & CI/CD (JAX)"),
        GEN_AI("Generative AI (JAX)"),
        MICRO("Microservices & Modularisierung (JAX)"),
        PERF_SEC("Performance & Security (JAX)"),
        SERVER_JAVA("Serverside Java (JAX)"),
        WEB_JS("Web Development & JavaScript (JAX)");

        private final String displayName;

        Track(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
