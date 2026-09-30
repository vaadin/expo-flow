package com.example.application.views.devoxx;

import com.example.application.devoxx.Talk;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.ai.mcp.AliasRegistry;
import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.mcp.CatalogAccess;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.draiv.session.DemoPrincipalUiResolver;
import com.vaadin.draiv.session.UiRegistry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the search form through draiv's tools exactly as the LLM does — {@code inspect}, then
 * {@code apply} — just without a model in the loop. If this passes, the AI has everything it needs.
 */
@SpringBootTest
class DevoxxTalksDrivingTest extends SpringBrowserlessTest {

    private final UiRegistry registry = new UiRegistry();
    private final CapabilityMcpTools tools = new CapabilityMcpTools(
            new CatalogAccess(new DemoPrincipalUiResolver(registry), new AliasRegistry()));

    @Test
    void aiToolsCanFillTheFormAndSearch() {
        navigate(DevoxxTalksView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        String snapshot = tools.inspect(null, null, null);

        // What the LLM would send for "Security talks on Wednesday morning"
        apply(snapshot, "filter-tracks", "settable", "set", "Security");
        apply(snapshot, "filter-starts-after", "settable", "set", "2026-10-07T09:00");
        apply(snapshot, "filter-starts-before", "settable", "set", "2026-10-07T12:00");
        apply(snapshot, "search-button", "activatable", "activate");

        var talks = shownTalks();
        assertFalse(talks.isEmpty());
        assertTrue(talks.stream().allMatch(talk -> talk.track().equals("Security, Trust & Compliance")
                && !talk.start().isBefore(LocalDateTime.of(2026, 10, 7, 9, 0))
                && !talk.start().isAfter(LocalDateTime.of(2026, 10, 7, 12, 0))));
    }

    private void apply(String snapshot, String id, String capability, String operation, String... args) {
        var result = tools.apply(selectorFor(snapshot, id), capability, operation, List.of(args), null);
        assertEquals("ok", result.status(), () -> id + ": " + result);
    }

    /** The {@code sel=#n} handle that {@code inspect} printed for the component with this id. */
    private static String selectorFor(String snapshot, String id) {
        return snapshot.lines()
                .filter(line -> line.contains("id=\"" + id + "\"") && line.contains("sel="))
                .map(line -> line.substring(line.indexOf("sel=") + "sel=".length()).split("\\s")[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("No " + id + " in snapshot:\n" + snapshot));
    }

    @SuppressWarnings("unchecked")
    private List<Talk> shownTalks() {
        Grid<Talk> grid = $(Grid.class).single();
        return grid.getListDataView().getItems().toList();
    }
}
