package com.example.application.ai;

import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.draiv.script.Input;
import com.vaadin.draiv.script.Script;
import com.vaadin.draiv.script.ScriptRunner;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.script.Step;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tab 5: the AI once, then without it. Asked for a macro, the AI writes a draiv script with
 * {@code save_script} and runs it once with {@code run_script}; from then on "Ausführen" replays it
 * through draiv's {@link ScriptRunner}, with no model involved.
 */
@PageTitle("Makro")
@Route("ai/macros")
public class AiMacroView extends AiTabView {

    private static final Logger log = LoggerFactory.getLogger(AiMacroView.class);

    /**
     * How the AI should write a macro for THIS app. Filled in with the stored macros and the request.
     * draiv's own prompt already tells the model today's date.
     */
    static final String GOAL = """
            The user wants a macro: a draiv script that repeats work in this app without AI. Write it, \
            save it with save_script, then run it once with run_script so the user sees it work. Do not \
            operate the UI with apply yourself.

            Invoices are entered with the form on route ai/upload. Field ids: invoice-number, \
            invoice-supplier, invoice-date and invoice-due-date (ISO yyyy-MM-dd), invoice-net (a plain \
            number like 1000 or 249.90), invoice-vat-rate ("0 %%", "7 %%" or "19 %%"), invoice-category \
            (Hardware, Software, Reise, Marketing or Büro), invoice-receipt (optional file name). The \
            button invoice-add-button (Hinzufügen) saves one invoice and empties the form; \
            invoice-reset-button empties it.

            Script rules: steps are navigate (to "ai/upload"), set (by "id") and click (by "id"). Use fixed \
            values, or ${input} placeholders when the user asks for something variable. NEVER use an \
            assist step: a macro must run without AI. Write dates as fixed ISO dates. Name the script in \
            short kebab-case, e.g. "drei-testrechnungen", and describe it in one German sentence. Test \
            data should look like test data, e.g. invoice numbers TEST-001, TEST-002.

            Macros stored so far: %s. Reply in one short German sentence. Request: "%s"
            """;

    private final ScriptStore scripts;
    private final Grid<Script> grid = new Grid<>();
    private final Runnable onChange = this::refreshLater;
    private UI ui;

    public AiMacroView(UiAssistant assistant, ScriptStore scripts) {
        super("Makro",
                "Einmal mit AI, danach ohne: Die AI schreibt ein Makro und führt es aus. Danach läuft es per Klick, ganz ohne Modell.");
        this.scripts = scripts;

        // No scope: the AI's run_script navigates to the form, which a scope would refuse
        var prompt = AiPromptBar.singleLine(assistant, null, this::aiGoal, "Makro erstellen");
        prompt.getPrompt().setPlaceholder("z. B. „Lege drei Testrechnungen an und speichere das als Makro“");
        prompt.addFallback(FallbackMacros.MACRO);

        grid.addColumn(Script::name).setHeader("Makro").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Script::description).setHeader("Beschreibung").setFlexGrow(1);
        grid.addColumn(script -> script.steps().size()).setHeader("Schritte").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(this::actions)).setAutoWidth(true).setFlexGrow(0);
        // Click a macro to read its steps: what the AI wrote, in plain text
        grid.setItemDetailsRenderer(new ComponentRenderer<>(script -> new Pre(describe(script))));
        grid.setAllRowsVisible(true);
        grid.setEmptyStateText("Noch keine Makros. Bitte die AI um eins.");
        grid.setId("macro-grid");
        // People run the macros; the AI has run_script for that
        grid.getElement().setAttribute("data-draiv-control", "true");

        add(prompt, grid);
        refresh();
    }

    private String aiGoal(String request) {
        return GOAL.formatted(scripts.list().isEmpty() ? "none" : String.join(", ", scripts.list()), request);
    }

    private HorizontalLayout actions(Script script) {
        var run = new Button("Ausführen", VaadinIcon.PLAY.create(), e -> runOrAskForInputs(script));
        run.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.SMALL);
        var delete = new Button(VaadinIcon.TRASH.create(), e -> scripts.delete(script.name()));
        delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        delete.setAriaLabel("Makro löschen");
        delete.setTooltipText("Makro löschen");
        return new HorizontalLayout(run, delete);
    }

    private void runOrAskForInputs(Script script) {
        if (script.inputs() == null || script.inputs().isEmpty()) {
            run(script, Map.of());
            return;
        }
        var dialog = new Dialog("Makro „" + script.name() + "“");
        var form = new FormLayout();
        var fields = new LinkedHashMap<String, TextField>();
        for (Input input : script.inputs()) {
            var field = new TextField(input.name());
            if (input.defaultValue() != null) {
                field.setValue(input.defaultValue());
            }
            fields.put(input.name(), field);
            form.add(field);
        }
        var run = new Button("Ausführen", e -> {
            dialog.close();
            run(script, fields.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));
        });
        run.addThemeVariants(ButtonVariant.PRIMARY);
        dialog.add(form);
        dialog.getFooter().add(new Button("Abbrechen", e -> dialog.close()), run);
        dialog.open();
    }

    /** Replays the macro on this UI, step by step, without a model. */
    private void run(Script script, Map<String, Object> args) {
        var currentUi = UI.getCurrent();
        long start = System.nanoTime();
        ScriptRunner.runAsync(script, args, currentUi).whenComplete((runId, error) -> currentUi.access(() -> {
            if (error != null) {
                log.warn("Macro {} failed", script.name(), error);
                Notification.show("Makro fehlgeschlagen: " + error.getMessage())
                        .addThemeVariants(NotificationVariant.ERROR);
            } else {
                Notification.show("Makro „%s“ ausgeführt, ohne AI, in %d ms.".formatted(
                        script.name(), (System.nanoTime() - start) / 1_000_000))
                        .addThemeVariants(NotificationVariant.SUCCESS);
            }
        }));
    }

    /** The steps as one line each, e.g. {@code set invoice-supplier = Acme GmbH}. */
    static String describe(Script script) {
        return script.steps().stream().map(step -> switch (step) {
            case Step.Navigate navigate -> "navigate " + navigate.to();
            case Step.Set set -> "set " + target(set.id(), set.label()) + " = " + set.value();
            case Step.Click click -> "click " + target(click.id(), click.label());
            case Step.Read read -> "read " + target(read.id(), read.label()) + " -> " + read.name();
            case Step.Assist assist -> "assist (AI) " + assist.goal();
            case Step.Apply apply -> "apply " + target(apply.id(), apply.label()) + " " + apply.capability()
                    + "/" + apply.operation() + " " + apply.args();
        }).collect(Collectors.joining("\n"));
    }

    private static String target(String id, String label) {
        return id != null && !id.isBlank() ? id : "„" + label + "“";
    }

    private void refresh() {
        grid.setItems(scripts.list().stream().map(scripts::load).toList());
    }

    /** The store calls from whichever thread saved, often the AI's: hop onto this UI. */
    private void refreshLater() {
        if (ui != null) {
            ui.access(this::refresh);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ui = attachEvent.getUI();
        scripts.addChangeListener(onChange);
        refresh();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        scripts.removeChangeListener(onChange);
        ui = null;
        super.onDetach(detachEvent);
    }
}
