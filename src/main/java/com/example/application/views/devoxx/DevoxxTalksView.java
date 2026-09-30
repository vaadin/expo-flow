package com.example.application.views.devoxx;

import com.example.application.devoxx.DevoxxTalkService;
import com.example.application.devoxx.Talk;
import com.example.application.devoxx.TalkFilter;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.flow.automation.indication.ChangeIndication;
import com.vaadin.flow.automation.indication.ChangeIndication.IndicationStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Find talks at Devoxx Belgium 2026 by asking in plain language: the AI fills in the search form
 * and clicks Search (see {@link AiSearchBar}), and the fields it changed flash briefly.
 */
@PageTitle("Devoxx Talks")
@Route("devoxx")
@Menu(title = "Devoxx Talks", icon = LineAwesomeIconUrl.CALENDAR_SOLID, order = 0)
public class DevoxxTalksView extends VerticalLayout {

    private static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("EEE HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter NOW = DateTimeFormatter.ofPattern("EEEE yyyy-MM-dd HH:mm", Locale.ENGLISH);

    /**
     * How the AI should operate THIS view. Filled in with the current time, the previous request, the
     * tracks, the formats and the request.
     */
    private static final String AI_GOAL = """
            The user asked about talks at Devoxx Belgium 2026 (Antwerp, Monday 5 to Friday 9 October 2026). \
            It is now %s, conference time. Answer by operating the talk search form on screen: set the \
            filter fields the request implies, then click the form's Search button (id search-button).

            The previous request was: "%s". If the new request only adds or changes a criterion \
            ("only for beginners", "nur Deep Dives", "and on Friday?", "without AI"), it refines that \
            search: keep every field that is already set and change only what the request mentions. \
            Only when it asks for different talks altogether, click Reset (id reset-button) first.

            Map the request to the form:
            - Tracks: %s. When one of these tracks covers the topic asked for (e.g. "security"), \
            select that track and leave Topics empty.
            - Topics: for other topics. Comma-separated words, matched as whole words in title, track, \
            keywords and description. Add common variants, e.g. "test, tests, testing".
            - Exclude topics: the same, for what the user does not want. "Without AI" means \
            "AI, LLM, LLMs, GenAI, agent, agents, agentic, MCP, RAG".
            - Speaker: part of a speaker's name.
            - Format: %s.
            - Level: Beginner, Intermediate, Advanced.
            - Starts after / Starts before: "next", "coming up" or "now" means from now until 60 minutes \
            from now. A day or part of a day ("Thursday afternoon") means that time window on that date.

            Be quick, every tool round costs time: inspect once, then make all changes and the Search \
            click in a single apply_many, and do not inspect again afterwards. Set fields with capability \
            "settable", operation "set" (multi-selects take a list of option labels, Starts after / \
            before an ISO date-time like 2026-10-08T12:00); click buttons with "activatable", "activate".

            Do not invent constraints the user did not ask for. The user may write in any language. \
            Keep your final answer to one short sentence. Request: "%s"
            """;

    private final DevoxxTalkService talkService;
    private final Grid<Talk> grid = new Grid<>();
    private final Span talkCount = new Span();
    // Each AI run starts without memory, so pass on the previous request to allow "only for beginners"
    private String previousRequest = "";

    public DevoxxTalksView(DevoxxTalkService talkService, TextPresenterService presenter,
                           TextPresenterProperties presenterProperties) {
        this.talkService = talkService;

        // Briefly highlight every field the AI changes
        ChangeIndication.setStyle(UI.getCurrent(), IndicationStyle.FLASH);

        grid.addColumn(talk -> talk.start().format(DAY_AND_TIME) + "–" + talk.end().format(TIME))
                .setHeader("When").setComparator(Talk::start).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Talk::title).setHeader("Title").setFlexGrow(4);
        grid.addColumn(Talk::track).setHeader("Track").setSortable(true).setFlexGrow(1);
        // Click a row to read the abstract. A template renderer: no server-side component per row.
        // talk-facts (devoxx-view.css) gives Author, Format and Room equal columns in every talk
        grid.setItemDetailsRenderer(LitRenderer.<Talk>of("""
                        <div class="talk-facts">
                            <span><b>Author:</b> ${item.speakers}</span>
                            <span><b>Format:</b> ${item.format}</span>
                            <span><b>Room:</b> ${item.room}</span>
                        </div>
                        <p>${item.description}</p>
                        """)
                .withProperty("speakers", Talk::speakers)
                .withProperty("format", Talk::format)
                .withProperty("room", Talk::room)
                .withProperty("description", Talk::description));
        // The details follow the selection: a click opens a talk, a second click closes it
        grid.setDetailsVisibleOnClick(false);
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getOldValue() != null) {
                grid.setDetailsVisible(event.getOldValue(), false);
            }
            if (event.getValue() != null) {
                grid.setDetailsVisible(event.getValue(), true);
            }
        });
        // Arrow up / down selects the talk the focus moved to. It sets the grid's activeItem in the
        // browser, just like a click, so clicks and keys never fight over the selection. vaadin-grid
        // also makes open details a keyboard stop of their own: when the focus lands there, press the
        // key once more, so it goes straight to the neighbouring talk
        grid.getElement().executeJs("""
                this.addEventListener('keydown', e => {
                    if (e.key !== 'ArrowDown' && e.key !== 'ArrowUp') {
                        return;
                    }
                    const cell = this.shadowRoot.activeElement;
                    if (cell?.matches('[part~="details-cell"]')) {
                        cell.dispatchEvent(new KeyboardEvent('keydown', {key: e.key, bubbles: true, composed: true}));
                    } else if (cell?.parentElement?._item) {
                        this.activeItem = cell.parentElement._item;
                    }
                });
                """);
        grid.addThemeVariants(GridVariant.WRAP_CELL_CONTENT);
        grid.setSizeFull();

        add(new AiSearchBar(presenter, presenterProperties, this::aiGoal),
                new TalkSearchForm(talkService, this::showTalks),
                talkCount,
                grid);
        setSizeFull();

        showTalks(TalkFilter.empty());
    }

    private String aiGoal(String request) {
        var goal = AI_GOAL.formatted(talkService.conferenceTime().format(NOW),
                previousRequest.isEmpty() ? "none" : previousRequest,
                String.join("; ", talkService.tracks()), // track names contain commas
                String.join(", ", talkService.formats()),
                request);
        previousRequest = request;
        return goal;
    }

    private void showTalks(TalkFilter filter) {
        var talks = talkService.findTalks(filter);
        grid.setItems(talks);
        talkCount.setText(talks.size() + " of " + talkService.countTalks() + " talks");
    }
}
