package com.example.application.devoxx;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Devoxx Belgium 2026 talks, loaded once on startup from the public CFP API
 * (<a href="https://dvbe26.cfp.dev/swagger-ui/index.html">dvbe26.cfp.dev</a>). When the API cannot be
 * reached — conference Wi-Fi — the copy bundled in {@code src/main/resources/devoxx/} is used instead.
 */
@Service
public class DevoxxTalkService {

    private static final Logger log = LoggerFactory.getLogger(DevoxxTalkService.class);

    public static final ZoneId CONFERENCE_TIME_ZONE = ZoneId.of("Europe/Brussels");
    private static final List<String> DAYS = List.of("monday", "tuesday", "wednesday", "thursday", "friday");

    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String scheduleUrl;
    private final LocalDateTime demoTime;
    private final List<Talk> talks;

    public DevoxxTalkService(RestClient.Builder restClientBuilder, JsonMapper jsonMapper,
                             @Value("${devoxx.schedule-url:}") String scheduleUrl,
                             @Value("${devoxx.demo-time:}") String demoTime) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.jsonMapper = jsonMapper;
        this.scheduleUrl = scheduleUrl;
        this.demoTime = demoTime.isBlank() ? null : LocalDateTime.parse(demoTime);

        talks = DAYS.stream()
                .flatMap(day -> loadSchedule(day).stream())
                .filter(slot -> slot.proposal() != null) // breaks, lunch, ...
                .map(DevoxxTalkService::toTalk)
                .sorted(Comparator.comparing(Talk::start))
                .toList();
        log.info("Loaded {} Devoxx talks", talks.size());
    }

    public List<Talk> findTalks(TalkFilter filter) {
        return talks.stream().filter(filter::matches).toList();
    }

    public int countTalks() {
        return talks.size();
    }

    public List<String> tracks() {
        return talks.stream().map(Talk::track).filter(track -> !track.isEmpty()).distinct().sorted().toList();
    }

    public List<String> formats() {
        return talks.stream().map(Talk::format).distinct().sorted().toList();
    }

    public List<String> levels() {
        return List.of("Beginner", "Intermediate", "Advanced");
    }

    /** The current conference time, or the configured {@code devoxx.demo-time} when rehearsing. */
    public LocalDateTime conferenceTime() {
        return demoTime != null ? demoTime : LocalDateTime.now(CONFERENCE_TIME_ZONE);
    }

    private List<ScheduleSlot> loadSchedule(String day) {
        if (!scheduleUrl.isBlank()) {
            try {
                return restClient.get().uri(scheduleUrl + day).retrieve().body(new ParameterizedTypeReference<>() {});
            } catch (RestClientException e) {
                log.warn("Devoxx API not reachable, using the bundled {} schedule: {}", day, e.getMessage());
            }
        }
        return jsonMapper.readValue(getClass().getResourceAsStream("/devoxx/" + day + ".json"), new TypeReference<>() {});
    }

    private static Talk toTalk(ScheduleSlot slot) {
        var proposal = slot.proposal();
        return new Talk(
                proposal.title().strip(),
                proposal.speakers().stream().map(Speaker::fullName).collect(Collectors.joining(", ")),
                nameOf(proposal.track()),
                nameOf(proposal.sessionType()),
                capitalize(proposal.audienceLevel()), // BEGINNER -> Beginner
                nameOf(slot.room()),
                LocalDateTime.ofInstant(slot.fromDate(), CONFERENCE_TIME_ZONE),
                LocalDateTime.ofInstant(slot.toDate(), CONFERENCE_TIME_ZONE),
                proposal.keywords().stream().map(Named::name).toList(),
                Jsoup.parse(Objects.requireNonNullElse(proposal.description(), "")).text()); // HTML -> plain text
    }

    private static String nameOf(Named named) {
        return named == null ? "" : named.name();
    }

    private static String capitalize(String text) {
        return text == null || text.isEmpty() ? "" : text.charAt(0) + text.substring(1).toLowerCase();
    }

    // The parts of the CFP API's JSON this app uses. Unknown properties are ignored.

    record ScheduleSlot(Instant fromDate, Instant toDate, Named room, Proposal proposal) {
    }

    record Proposal(String title, String description, String audienceLevel, Named track, Named sessionType,
                    List<Speaker> speakers, List<Named> keywords) {
    }

    record Speaker(String fullName) {
    }

    record Named(String name) {
    }
}
