package com.example.application.views.devoxx;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.ai.presenter.TextPresenterService.AssistResult;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Natural-language search, following draiv's vaadin-grid-search-ui example. The text is not turned
 * into a query here: the draiv presenter hands a goal to the LLM, and the LLM operates the search UI
 * on screen through Spring AI tool calls — inspect the component tree, fill in fields, click Search.
 */
public class AiSearchBar extends HorizontalLayout {

    private static final Logger log = LoggerFactory.getLogger(AiSearchBar.class);

    private final TextPresenterService presenter;
    private final TextPresenterProperties presenterProperties;
    private final Function<String, String> goalForRequest;
    private final TextField prompt = new TextField();
    private final Button ask = new Button("Ask AI", VaadinIcon.MAGIC.create());

    /**
     * @param goalForRequest turns the user's words into the goal for the AI: how to operate this view
     */
    public AiSearchBar(TextPresenterService presenter, TextPresenterProperties presenterProperties,
                       Function<String, String> goalForRequest) {
        this.presenter = presenter;
        this.presenterProperties = presenterProperties;
        this.goalForRequest = goalForRequest;

        prompt.setPlaceholder("Ask in plain language, e.g. \"What's next, but nothing about AI?\"");
        prompt.setClearButtonVisible(true);
        prompt.setAutoselect(true);

        ask.addThemeVariants(ButtonVariant.PRIMARY);
        ask.addClickShortcut(Key.ENTER).listenOn(prompt);
        ask.addClickListener(e -> askAi());

        add(prompt, ask);
        setFlexGrow(1, prompt);
        setWidthFull();
    }

    private void askAi() {
        if (prompt.isEmpty()) {
            return;
        }
        if (!presenterProperties.isEnabled()) {
            Notification.show("AI search is off: no API key for provider '" + presenterProperties.provider() + "'")
                    .addThemeVariants(NotificationVariant.ERROR);
            return;
        }
        var ui = UI.getCurrent();
        var principal = DemoPrincipal.forUi(ui); // tells draiv which browser tab to drive
        var goal = goalForRequest.apply(prompt.getValue().strip());

        ask.setEnabled(false);
        ask.setText("Thinking…");
        // The model and tool loop takes a few seconds and drives this UI through ui.access(), so it must
        // not run here on the request thread, which holds the session lock
        CompletableFuture.supplyAsync(() -> presenter.assist(principal, goal))
                .whenComplete((result, error) -> ui.access(() -> {
                    ask.setEnabled(true);
                    ask.setText("Ask AI");
                    if (error != null) {
                        log.warn("AI search failed", error);
                        Notification.show("AI search failed: " + error.getMessage())
                                .addThemeVariants(NotificationVariant.ERROR);
                    } else {
                        logToolCalls(result);
                        Notification.show(result.summary() == null || result.summary().isBlank()
                                ? "AI updated the search" : result.summary());
                    }
                }));
    }

    /** Shows in the console which tools the AI called, to explain what happened. */
    private static void logToolCalls(AssistResult result) {
        log.info("AI made {} tool call(s)", result.toolCalls().size());
        result.toolCalls().forEach(call -> log.info("  {} {}", call.name(), call.args()));
    }
}
