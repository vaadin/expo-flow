package com.example.application.ai;

import com.vaadin.draiv.script.Script;
import com.vaadin.draiv.script.ScriptRunner;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * "Ohne AI vorführen": replays a tab's {@link FallbackMacros fallback macro}, for when the network or the
 * model lets the booth down. Same result as the AI's run for the example prompt, without a model.
 */
public class FallbackButton extends Button {

    private static final Logger log = LoggerFactory.getLogger(FallbackButton.class);

    public FallbackButton(Script fallback) {
        super("Ohne AI vorführen", VaadinIcon.PLAY.create());
        addThemeVariants(ButtonVariant.TERTIARY);
        setTooltipText(fallback.description());
        addClickListener(e -> run(fallback));
        // Driving chrome: the AI never presses it
        getElement().setAttribute("data-draiv-control", "true");
    }

    private static void run(Script fallback) {
        var ui = UI.getCurrent();
        ScriptRunner.runAsync(fallback, Map.of(), ui).whenComplete((runId, error) -> ui.access(() -> {
            if (error != null) {
                log.warn("Fallback {} failed", fallback.name(), error);
                Notification.show("Fallback fehlgeschlagen: " + error.getMessage())
                        .addThemeVariants(NotificationVariant.ERROR);
            }
        }));
    }
}
