package com.example.application.views.devoxx;

import com.example.application.devoxx.DevoxxTalkService;
import com.example.application.devoxx.Talk;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Uses the form by hand, like a visitor would. Runs on the bundled schedule (see test application.properties). */
@SpringBootTest
class DevoxxTalksViewTest extends SpringBrowserlessTest {

    @Autowired
    DevoxxTalkService talkService;

    @Test
    void showsAllTalksInitially() {
        navigate(DevoxxTalksView.class);

        assertEquals(204, talkService.countTalks());
        assertEquals(talkService.countTalks(), shownTalks().size());
    }

    @Test
    void filtersByTrackAndLevel() {
        navigate(DevoxxTalksView.class);

        test($(MultiSelectComboBox.class).id("filter-tracks")).selectItem("Security, Trust & Compliance");
        test($(MultiSelectComboBox.class).id("filter-levels")).selectItem("Beginner");
        test($(Button.class).id("search-button")).click();

        var talks = shownTalks();
        assertFalse(talks.isEmpty());
        assertTrue(talks.stream().allMatch(talk ->
                talk.track().equals("Security, Trust & Compliance") && talk.level().equals("Beginner")));
    }

    @Test
    void excludedTopicsLeaveOutAiTalks() {
        navigate(DevoxxTalksView.class);

        test($(TextField.class).id("filter-excluded-topics")).setValue("AI, LLM, agents");
        test($(Button.class).id("search-button")).click();

        var talks = shownTalks();
        assertFalse(talks.isEmpty());
        assertTrue(talks.size() < talkService.countTalks());
        assertTrue(talks.stream().noneMatch(talk -> talk.title().matches("(?i).*\\b(AI|LLM|agents)\\b.*")));
    }

    @Test
    void filtersByStartTime() {
        navigate(DevoxxTalksView.class);
        var from = LocalDateTime.of(2026, 10, 7, 10, 0);
        var until = from.plusMinutes(30);

        test($(DateTimePicker.class).id("filter-starts-after")).setValue(from);
        test($(DateTimePicker.class).id("filter-starts-before")).setValue(until);
        test($(Button.class).id("search-button")).click();

        var talks = shownTalks();
        assertFalse(talks.isEmpty());
        assertTrue(talks.stream().allMatch(talk -> !talk.start().isBefore(from) && !talk.start().isAfter(until)));
    }

    @Test
    void resetShowsAllTalksAgain() {
        navigate(DevoxxTalksView.class);
        test($(TextField.class).id("filter-topics")).setValue("java");
        test($(Button.class).id("search-button")).click();
        assertTrue(shownTalks().size() < talkService.countTalks());

        test($(Button.class).id("reset-button")).click();

        assertEquals(talkService.countTalks(), shownTalks().size());
        assertTrue($(TextField.class).id("filter-topics").isEmpty());
    }

    @SuppressWarnings("unchecked")
    private List<Talk> shownTalks() {
        Grid<Talk> grid = $(Grid.class).single();
        return grid.getListDataView().getItems().toList();
    }
}
