package com.example.application.ai;

import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * A prompt and a button: the words become a goal, and draiv's {@link UiAssistant} has the AI carry it out
 * on this view, limited to an {@link AgentScope}. The changed fields glow.
 * <p>
 * Like draiv's {@code AiGoalBar}, except that the keyboard shortcut listens on the prompt only: the
 * {@code AiGoalBar} puts Enter on the whole UI, so Enter in any form field or in the chat would start the
 * AI as well.
 */
public class AiPromptBar extends Composite<HorizontalLayout> {

    private static final Logger log = LoggerFactory.getLogger(AiPromptBar.class);

    private final UiAssistant assistant;
    private final AgentScope scope;
    private final Function<String, String> goalForRequest;
    private final TextFieldBase<?, String> prompt;
    private final Button button;
    private final String buttonText;

    private AiPromptBar(UiAssistant assistant, AgentScope scope, Function<String, String> goalForRequest,
                        TextFieldBase<?, String> prompt, String buttonText) {
        this.assistant = assistant;
        this.scope = scope;
        this.goalForRequest = goalForRequest;
        this.prompt = prompt;
        this.buttonText = buttonText;
        this.button = new Button(buttonText, VaadinIcon.MAGIC.create(), e -> run());

        prompt.setWidthFull();
        button.addThemeVariants(ButtonVariant.PRIMARY);
        var layout = getContent();
        layout.add(prompt, button);
        layout.setFlexGrow(1, prompt);
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setWidthFull();
        // Driving chrome: the AI never sees or presses the controls that start it
        getElement().setAttribute("data-draiv-control", "true");
    }

    /** A one-line prompt; Enter runs it. */
    public static AiPromptBar singleLine(UiAssistant assistant, AgentScope scope,
                                         Function<String, String> goalForRequest, String buttonText) {
        var bar = new AiPromptBar(assistant, scope, goalForRequest, new TextField(), buttonText);
        bar.button.addClickShortcut(Key.ENTER).listenOn(bar.prompt);
        return bar;
    }

    /** A text area for pasted text, an e-mail say; Ctrl+Enter runs it. */
    public static AiPromptBar multiLine(UiAssistant assistant, AgentScope scope,
                                        Function<String, String> goalForRequest, String buttonText) {
        var area = new TextArea();
        area.setMinRows(3);
        var bar = new AiPromptBar(assistant, scope, goalForRequest, area, buttonText);
        bar.button.addClickShortcut(Key.ENTER, KeyModifier.CONTROL).listenOn(area);
        return bar;
    }

    public TextFieldBase<?, String> getPrompt() {
        return prompt;
    }

    private void run() {
        if (prompt.isEmpty()) {
            Notification.show("Erst etwas eingeben.");
            return;
        }
        if (!assistant.isEnabled()) {
            Notification.show(assistant.disabledReason()).addThemeVariants(NotificationVariant.ERROR);
            return;
        }
        var ui = UI.getCurrent();
        var goal = goalForRequest.apply(prompt.getValue().strip());
        setBusy(true);
        assistant.assist(goal, scope, null).whenComplete((result, error) -> ui.access(() -> {
            setBusy(false);
            if (error != null) {
                log.warn("AI run failed", error);
                Notification.show("AI fehlgeschlagen: " + error.getMessage())
                        .addThemeVariants(NotificationVariant.ERROR);
            } else {
                log.info("AI made {} tool call(s)", result.toolCalls().size());
                result.toolCalls().forEach(call -> log.info("  {} {}", call.name(), call.args()));
                Notification.show(result.summary() == null || result.summary().isBlank()
                        ? "Erledigt." : result.summary());
            }
        }));
    }

    private void setBusy(boolean busy) {
        button.setEnabled(!busy);
        button.setText(busy ? "Moment …" : buttonText);
    }
}
