package com.example.application.ai;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.IntSupplier;

/**
 * Filters folded away in a {@link Details}, with a badge counting the active ones, like the filters in
 * {@code WjaxTalksView}. Unlike there, the block stays closed: only a person opens it. While closed, nobody
 * sees the changed fields flash, so the summary pulses on every change instead, whether the AI or a person
 * made it. draiv drives the fields all the same: closed, they stay attached and visible server-side.
 */
public class FilterDetails extends Details {

    private final HorizontalLayout summary;
    private final Badge activeFilters = new Badge("aktive Filter", 0);
    private final IntSupplier activeCount;
    private boolean pulsePending;
    private boolean alternate;

    /**
     * @param activeCount how many filters constrain the result right now, for the badge
     * @param watched     the fields whose changes make the summary pulse
     */
    public FilterDetails(Component filters, IntSupplier activeCount, HasValue<?, ?>... watched) {
        this.activeCount = activeCount;
        activeFilters.addThemeVariants(BadgeVariant.NUMBER_ONLY, BadgeVariant.SMALL);
        summary = new HorizontalLayout(new Span("Filter"), activeFilters);
        summary.addClassName("filter-summary");
        setSummary(summary);
        add(filters);
        setWidthFull();
        addClassName("filter-details");
        for (HasValue<?, ?> field : watched) {
            field.addValueChangeListener(e -> changed());
        }
        updateBadge();
    }

    private void changed() {
        updateBadge();
        // One pulse per server round trip, however many fields the AI set in it
        if (!pulsePending) {
            pulsePending = true;
            getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(this, context -> pulse()));
        }
    }

    private void updateBadge() {
        int active = activeCount.getAsInt();
        activeFilters.setNumber(active);
        activeFilters.setVisible(active > 0);
    }

    /**
     * Restarts the CSS animation: switching between two classes that run the same animation restarts it,
     * where removing and re-adding one class in the same response would not.
     */
    private void pulse() {
        pulsePending = false;
        alternate = !alternate;
        summary.removeClassNames("filter-pulse-a", "filter-pulse-b");
        summary.addClassName(alternate ? "filter-pulse-a" : "filter-pulse-b");
    }
}
