package com.example.application.views.devoxx;

import com.example.application.devoxx.DevoxxTalkService;
import com.example.application.devoxx.TalkFilter;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

/**
 * An ordinary search form over the talks. The AI search fills in these very fields and clicks
 * Search, so a question in plain language turns into criteria the user can see and keep editing.
 */
public class TalkSearchForm extends FormLayout {

    private final TextField topics = new TextField("Topics");
    private final TextField excludedTopics = new TextField("Exclude topics");
    private final TextField speaker = new TextField("Speaker");
    private final MultiSelectComboBox<String> tracks = new MultiSelectComboBox<>("Tracks");
    private final MultiSelectComboBox<String> formats = new MultiSelectComboBox<>("Format");
    private final MultiSelectComboBox<String> levels = new MultiSelectComboBox<>("Level");
    private final DateTimePicker startsAfter = new DateTimePicker("Starts after");
    private final DateTimePicker startsBefore = new DateTimePicker("Starts before");

    private final Consumer<TalkFilter> onSearch;

    public TalkSearchForm(DevoxxTalkService talkService, Consumer<TalkFilter> onSearch) {
        this.onSearch = onSearch;

        topics.setPlaceholder("e.g. java, security");
        excludedTopics.setPlaceholder("e.g. AI, LLM");
        tracks.setItems(talkService.tracks());
        formats.setItems(talkService.formats());
        levels.setItems(talkService.levels());

        // Stable ids, so the AI (and the tests) can target each field precisely
        topics.setId("filter-topics");
        excludedTopics.setId("filter-excluded-topics");
        speaker.setId("filter-speaker");
        tracks.setId("filter-tracks");
        formats.setId("filter-formats");
        levels.setId("filter-levels");
        startsAfter.setId("filter-starts-after");
        startsBefore.setId("filter-starts-before");

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("600px", 2),
                new ResponsiveStep("1100px", 4));
        add(topics, excludedTopics, speaker, tracks, formats, levels, startsAfter, startsBefore);
        add(createButtons(), 4); // full width
    }

    private HorizontalLayout createButtons() {
        var search = new Button("Search", e -> onSearch.accept(buildFilter()));
        search.setId("search-button");
        search.addThemeVariants(ButtonVariant.PRIMARY);
        search.addClickShortcut(Key.ENTER).listenOn(this);

        var reset = new Button("Reset", e -> reset());
        reset.setId("reset-button");

        var buttons = new HorizontalLayout(search, reset);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }

    private TalkFilter buildFilter() {
        return new TalkFilter(topics.getValue(), excludedTopics.getValue(), speaker.getValue(),
                tracks.getValue(), formats.getValue(), levels.getValue(),
                startsAfter.getValue(), startsBefore.getValue());
    }

    /** Clears every field back to "no constraint" and shows all talks again. */
    private void reset() {
        topics.clear();
        excludedTopics.clear();
        speaker.clear();
        tracks.clear();
        formats.clear();
        levels.clear();
        startsAfter.clear();
        startsBefore.clear();
        onSearch.accept(TalkFilter.empty());
    }
}
