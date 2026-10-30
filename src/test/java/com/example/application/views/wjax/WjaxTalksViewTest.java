package com.example.application.views.wjax;

import com.example.application.wjax.Talk;
import com.example.application.wjax.WjaxTalkService;
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

/** Uses the form by hand, like a visitor would. Runs on the bundled W-JAX schedule. */
@SpringBootTest
class WjaxTalksViewTest extends SpringBrowserlessTest {

    @Autowired
    WjaxTalkService talkService;

    @Test
    void showsAllTalksInitially() {
        navigate(WjaxTalksView.class);

        assertEquals(119, talkService.countTalks()); // 148 sessions in the XML, 29 of them breaks
        assertEquals(talkService.countTalks(), shownTalks().size());
    }

    @Test
    void filtersByTrackAndLanguage() {
        navigate(WjaxTalksView.class);

        test($(MultiSelectComboBox.class).id("filter-tracks")).selectItem("Performance & Security");
        test($(MultiSelectComboBox.class).id("filter-languages")).selectItem("English");
        test($(Button.class).id("search-button")).click();

        var talks = shownTalks();
        assertFalse(talks.isEmpty());
        assertTrue(talks.stream().allMatch(talk ->
                talk.tracks().contains("Performance & Security") && talk.language().equals("English")));
    }

    @Test
    void excludedTopicsLeaveOutAiTalks() {
        navigate(WjaxTalksView.class);

        test($(TextField.class).id("filter-excluded-topics")).setValue("AI, LLM, agents");
        test($(Button.class).id("search-button")).click();

        var talks = shownTalks();
        assertFalse(talks.isEmpty());
        assertTrue(talks.size() < talkService.countTalks());
        assertTrue(talks.stream().noneMatch(talk -> talk.title().matches("(?i).*\\b(AI|LLM|agents)\\b.*")));
    }

    @Test
    void filtersByStartTime() {
        navigate(WjaxTalksView.class);
        var from = LocalDateTime.of(2026, 11, 4, 11, 30);
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
        navigate(WjaxTalksView.class);
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
