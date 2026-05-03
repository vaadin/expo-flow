package com.example.application.views;

import com.example.application.data.entity.Talk;
import com.example.application.data.repository.TalkRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Menu(title = "AI Filter", order = 10, icon = LineAwesomeIconUrl.TOOLS_SOLID)
@Route(value = "ai-filter")
public class AiFilterView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(AiFilterView.class);

    final Grid<Talk> grid;
    private final TalkRepository talkRepository;
    private final ChatClient chatClient;
    private final MessageList messageList;
    private final List<Talk> allTalks;

    public AiFilterView(TalkRepository talkRepository, ChatModel chatModel) {
        this.talkRepository = talkRepository;
        chatClient = ChatClient.builder(chatModel)
                .build();

        grid = new Grid<>(Talk.class);
        allTalks = talkRepository.findAll();

        grid.setItems(allTalks);
        grid.setColumns("title", "speaker", "room");
        grid.getColumnByKey("room").setFlexGrow(0).setSortable(true).setHeader("Room");
        grid.addColumn(talk ->
                        talk.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM. EEE")))
                .setFlexGrow(0).setHeader("Date").setSortable(true);
        grid.addColumn(talk ->
                        talk.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                                talk.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .setFlexGrow(0).setHeader("Time").setSortable(true);
        grid.addColumn(talk -> talk.getTracks().stream()
                        .map(Talk.Track::getDisplayName)
                        .collect(Collectors.joining(", ")))
                .setSortable(true).setHeader("Tracks");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        messageList = new MessageList();
        messageList.setMarkdown(true);
        var messageInput = new MessageInput(this::onSubmit);

        messageList.setSizeFull();
        messageInput.setWidthFull();

        add(grid, messageList, messageInput);
        setSpacing(true);
        setPadding(true);
        setSizeFull();
    }

    private void onSubmit(MessageInput.SubmitEvent event) {
        var question = event.getValue();
        var userMessage = new MessageListItem(question, Instant.now(), "User");
        var assistantMessage = new MessageListItem("", "Assistant");

        messageList.addItem(userMessage);
        messageList.addItem(assistantMessage);

        chatClient.prompt()
                .system("""
                        You are a helpful assistant that helps users find conference talks at JAX 2026 (May 4-8, 2026).
                        You control a grid that displays talks. Use the searchTalks tool to filter the grid based on the user's request.
                        Use showAllTalks to reset any active filter and show all talks again.
                        Available tracks (use exact enum names): AGILE, AGILE_FLOW, ARCH, CLOUD, CORE_JAVA, DATA_ML, DEVOPS, GEN_AI, MICRO, PERF_SEC, SERVER_JAVA, WEB_JS.
                        Available formats (use exact enum names): KEYNOTE, SESSION, WORKSHOP, PANEL, LAB, SHORTTALK.
                        Keep any return message as short as possible.
                        """)
                .user(question)
                .tools(this)
                .stream()
                .content()
                .subscribe(token -> {
                    getUI().ifPresent(ui -> ui.access(() -> assistantMessage.appendText(token)));
                });
    }

    private static final String TRACK_PARAM_DESCRIPTION = """                                                                                                                                       
      Track enum values to filter by, or null. Use the enum name in the list.                                                                                                                     
      Available tracks:                                                                                                                                                                           
        AGILE       = "Agile, People & Culture (JAX)"
        AGILE_FLOW  = "Agile Flow Day - Modern Productivity (JAX)"                                                                                                                                
        ARCH        = "Architecture & Design (JAX)"
        CLOUD       = "Clouds, Kubernetes & Serverless (JAX)"                                                                                                                                     
        CORE_JAVA   = "Core Java & Languages (JAX)"
        DATA_ML     = "Data & Machine Learning (JAX)"                                                                                                                                             
        DEVOPS      = "DevOps & CI/CD (JAX)"
        GEN_AI      = "Generative AI (JAX)"                                                                                                                                                       
        MICRO       = "Microservices & Modularisierung (JAX)"
        PERF_SEC    = "Performance & Security (JAX)"                                                                                                                                              
        SERVER_JAVA = "Serverside Java (JAX)"                                                                                                                                                     
        WEB_JS      = "Web Development & JavaScript (JAX)"
      Example: ["GEN_AI", "DATA_ML"]                                                                                                                                                              
      """;

    @Tool(description = """
            Search and filter conference talks shown in the grid. All parameters are optional — pass null to ignore.
            Tracks (exact enum names): AGILE, AGILE_FLOW, ARCH, CLOUD, CORE_JAVA, DATA_ML, DEVOPS, GEN_AI, MICRO, PERF_SEC, SERVER_JAVA, WEB_JS.
            Formats (exact enum names): KEYNOTE, SESSION, WORKSHOP, PANEL, LAB, SHORTTALK.
            Date format: yyyy-MM-dd (conference runs 2026-05-04 to 2026-05-08).
            Time format: HH:mm.
            Returns the number of matching talks now shown in the grid.
            """)
    int searchTalks(
            @ToolParam(description = "Part of the talk title to match, or null") String title,
            @ToolParam(description = "Part of the speaker name to match, or null") String speaker,
            @ToolParam(description = "Track enum values... \n" + TRACK_PARAM_DESCRIPTION) List<String> tracks,
            @ToolParam(description = "Date as yyyy-MM-dd, or null") String date,
            @ToolParam(description = "Earliest start time as HH:mm (inclusive), or null") String startTimeFrom,
            @ToolParam(description = "Latest start time as HH:mm (inclusive), or null") String startTimeTo,
            @ToolParam(description = "Room name or partial match, or null") String room,
            @ToolParam(description = "Talk format enum value, or null") String format
    ) {
        logger.info("searchTalks: title={}, speaker={}, tracks={}, date={}, startTimeFrom={}, startTimeTo={}, room={}, format={}",
                title, speaker, tracks, date, startTimeFrom, startTimeTo, room, format);
        var results = talkRepository.findAll(buildSpec(title, speaker, tracks, date, startTimeFrom, startTimeTo, room, format));
        getUI().ifPresent(ui -> ui.access(() -> grid.setItems(results)));
        return results.size();
    }

    @Tool(description = "Current date and time")
    LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }

    private Specification<Talk> buildSpec(String title, String speaker, List<String> tracks,
                                          String date, String startTimeFrom, String startTimeTo,
                                          String room, String format) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (speaker != null && !speaker.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("speaker")), "%" + speaker.toLowerCase() + "%"));
            }
            if (tracks != null && !tracks.isEmpty()) {
                List<Talk.Track> trackEnums = tracks.stream()
                        .map(t -> Talk.Track.valueOf(t.toUpperCase()))
                        .toList();
                query.distinct(true);
                predicates.add(root.join("tracks").in(trackEnums));
            }
            if (date != null && !date.isBlank()) {
                predicates.add(cb.equal(root.get("startDate"), LocalDate.parse(date)));
            }
            if (startTimeFrom != null && !startTimeFrom.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), LocalTime.parse(startTimeFrom)));
            }
            if (startTimeTo != null && !startTimeTo.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), LocalTime.parse(startTimeTo)));
            }
            if (room != null && !room.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("room")), "%" + room.toLowerCase() + "%"));
            }
            if (format != null && !format.isBlank()) {
                predicates.add(cb.equal(root.get("format"), Talk.Format.valueOf(format.toUpperCase())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    }
