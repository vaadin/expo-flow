package com.example.application.wjax;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The W-JAX 2026 talks, loaded once on startup from the conference planner's XML export bundled in
 * {@code src/main/resources/wjax/conference-schedule.xml}.
 */
@Service
public class WjaxTalkService {

    private static final Logger log = LoggerFactory.getLogger(WjaxTalkService.class);

    public static final ZoneId CONFERENCE_TIME_ZONE = ZoneId.of("Europe/Berlin");
    private static final String SCHEDULE = "/wjax/conference-schedule.xml";
    private static final Map<String, String> LANGUAGES = Map.of("de", "German", "en", "English");

    private final LocalDateTime demoTime;
    private final List<Talk> talks;

    public WjaxTalkService(@Value("${wjax.demo-time:}") String demoTime) throws Exception {
        this.demoTime = demoTime.isBlank() ? null : LocalDateTime.parse(demoTime);

        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        try (var in = getClass().getResourceAsStream(SCHEDULE)) {
            var root = factory.newDocumentBuilder().parse(in).getDocumentElement();
            // Sessions refer to their type, tracks and speakers by uniqueId
            var types = namesById(root, "sessionTypes", "sessiontype", e -> text(e, "name"));
            var tracks = namesById(root, "tracks", "track", e -> text(e, "name"));
            var speakers = namesById(root, "speakers", "speaker",
                    e -> (text(e, "firstName") + " " + text(e, "lastName")).strip());

            talks = children(child(root, "sessions"), "session").stream()
                    .filter(session -> types.containsKey(text(session, "type"))) // not breaks, lunch, ...
                    .map(session -> toTalk(session, types, tracks, speakers))
                    .sorted(Comparator.comparing(Talk::start))
                    .toList();
        }
        log.info("Loaded {} W-JAX talks", talks.size());
    }

    public List<Talk> findTalks(TalkFilter filter) {
        return talks.stream().filter(filter::matches).toList();
    }

    public int countTalks() {
        return talks.size();
    }

    public List<String> tracks() {
        return talks.stream().flatMap(talk -> talk.tracks().stream()).distinct().sorted().toList();
    }

    public List<String> formats() {
        return talks.stream().map(Talk::format).distinct().sorted().toList();
    }

    public List<String> languages() {
        return talks.stream().map(Talk::language).filter(language -> !language.isEmpty()).distinct().sorted().toList();
    }

    /** The current conference time, or the configured {@code wjax.demo-time} when rehearsing. */
    public LocalDateTime conferenceTime() {
        return demoTime != null ? demoTime : LocalDateTime.now(CONFERENCE_TIME_ZONE);
    }

    private static Talk toTalk(Element session, Map<String, String> types, Map<String, String> tracks,
                               Map<String, String> speakers) {
        // The primary track first, then the others
        var trackIds = new ArrayList<String>();
        trackIds.add(text(session, "primaryTrackUniqueId"));
        children(child(session, "tracks"), "track").stream()
                .map(track -> text(track, "trackId"))
                .filter(id -> !trackIds.contains(id))
                .forEach(trackIds::add);

        return new Talk(
                text(session, "name"),
                children(child(session, "speakers"), "speaker").stream()
                        .map(speaker -> speakers.getOrDefault(text(speaker, "speakerId"), ""))
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.joining(", ")),
                trackIds.stream().filter(tracks::containsKey).map(tracks::get).toList(),
                types.get(text(session, "type")),
                LANGUAGES.getOrDefault(text(session, "language"), ""),
                text(session, "roomName"),
                LocalDateTime.ofInstant(Instant.parse(text(session, "startDate")), CONFERENCE_TIME_ZONE),
                LocalDateTime.ofInstant(Instant.parse(text(session, "endDate")), CONFERENCE_TIME_ZONE),
                // HTML -> plain text. A few workshops add an agenda as the long abstract
                Jsoup.parse(text(session, "details") + " " + text(session, "longAbstract")).text());
    }

    // The XML nests elements of the same name (a session's <tracks> vs. the conference's <tracks>),
    // so these helpers only ever look at direct children.

    private static Map<String, String> namesById(Element root, String listTag, String itemTag,
                                                 Function<Element, String> name) {
        return children(child(root, listTag), itemTag).stream()
                .collect(Collectors.toMap(e -> text(e, "uniqueId"), name, (first, second) -> first));
    }

    private static List<Element> children(Element parent, String tag) {
        var result = new ArrayList<Element>();
        if (parent != null) {
            for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node instanceof Element element && element.getTagName().equals(tag)) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    private static Element child(Element parent, String tag) {
        var matches = children(parent, tag);
        return matches.isEmpty() ? null : matches.getFirst();
    }

    private static String text(Element parent, String tag) {
        var element = child(parent, tag);
        return element == null ? "" : element.getTextContent().strip();
    }
}
