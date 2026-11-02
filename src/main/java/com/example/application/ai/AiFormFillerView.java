package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceForm;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tab 2: paste a supplier's e-mail, and the AI fills in the invoice form from it. The AI may only
 * touch the form's fields ({@link AgentScope}): the status and the Add button are out of its reach,
 * so a person checks the result and saves it.
 * <p>
 * Not draiv's {@code AiGoalBar}: its Enter shortcut listens on the whole UI, so Enter in a form field
 * or in the chat would start the AI too. This is the same run through {@link UiAssistant}, triggered
 * by the button or Ctrl+Enter in the text area only.
 */
@PageTitle("Form Filler")
@Route("ai/form")
public class AiFormFillerView extends AiTabView {

    private static final Logger log = LoggerFactory.getLogger(AiFormFillerView.class);

    static final String EXAMPLE = """
            Hallo, anbei unsere Rechnung RE-2026-114 vom 28.10. für drei 27-Zoll-Monitore: \
            1.000 € netto zzgl. 19 % MwSt., fällig am 30.11.
            Viele Grüße, Acme GmbH""";

    /**
     * How the AI should operate THIS form; the %s receives the pasted text. draiv's own prompt
     * already tells the model today's date.
     */
    static final String GOAL = """
            The user pasted a supplier's e-mail or described an invoice. Enter that invoice in the \
            invoice form on screen, mapping the text to the fields:
            - invoice-number (Rechnungsnummer)
            - invoice-supplier (Lieferant): only one of the field's options. If the supplier is not \
            among them, leave the field empty and say so in your reply
            - invoice-date (Rechnungsdatum): ISO date, e.g. 2026-11-02; today if the text gives none
            - invoice-due-date (Fällig am): ISO date. A day without a year is the next such date from \
            the invoice date; "30 Tage" means 30 days after the invoice date
            - invoice-net (Netto): a plain number like 1000 or 1234.50. If only a gross amount is \
            given, net = gross / (1 + VAT rate), rounded to cents
            - invoice-vat-rate (MwSt-Satz): one of "0 %%", "7 %%", "19 %%"
            - invoice-category (Kategorie): Hardware, Software, Reise, Marketing or Büro, judged \
            from what was bought
            - invoice-receipt (Beleg): only if the text names a file
            Leave a field alone when the text says nothing about it. Do not click any button: a \
            person checks the form and saves it.

            Be quick, every tool round costs time: inspect once, then set all fields in a single \
            apply_many with capability "settable", operation "set", and do not inspect again. \
            Reply in one short German sentence; name any field you could not fill.

            Text: "%s"
            """;

    private final UiAssistant assistant;
    private final InvoiceForm form;
    private final AgentScope scope;
    private final TextArea text = new TextArea("Lieferanten-E-Mail");
    private final Button fill = new Button("Formular ausfüllen", VaadinIcon.MAGIC.create());

    public AiFormFillerView(UiAssistant assistant, InvoiceService invoiceService) {
        super("Form Filler",
                "Eine Lieferanten-E-Mail einfügen: Die AI füllt das Formular aus, speichern tut der Mensch.");
        this.assistant = assistant;
        this.form = new InvoiceForm(invoiceService);
        // The AI sees the form's fields, but neither the status nor the button that saves
        this.scope = AgentScope.within(form).excluding(form.getAddButton(), form.getStatusField());

        text.setPlaceholder("E-Mail oder Rechnungsdaten hier einfügen…");
        text.setWidthFull();
        text.setMinRows(3);
        text.setId("form-filler-text");

        fill.addThemeVariants(ButtonVariant.PRIMARY);
        fill.addClickListener(e -> fillForm());
        fill.addClickShortcut(Key.ENTER, KeyModifier.CONTROL).listenOn(text);
        var example = new Button("Beispiel-E-Mail", e -> text.setValue(EXAMPLE));
        example.addThemeVariants(ButtonVariant.TERTIARY);

        var buttons = new HorizontalLayout(fill, example);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        // Driving chrome: the AI never sees the controls that start it
        text.getElement().setAttribute("data-draiv-control", "true");
        buttons.getElement().setAttribute("data-draiv-control", "true");

        add(text, buttons, form);
    }

    private void fillForm() {
        if (text.isEmpty()) {
            Notification.show("Erst eine E-Mail einfügen.");
            return;
        }
        if (!assistant.isEnabled()) {
            Notification.show(assistant.disabledReason()).addThemeVariants(NotificationVariant.ERROR);
            return;
        }
        var ui = UI.getCurrent();
        setBusy(true);
        assistant.assist(GOAL.formatted(text.getValue().strip()), scope, null)
                .whenComplete((result, error) -> ui.access(() -> {
                    setBusy(false);
                    if (error != null) {
                        log.warn("Form filler failed", error);
                        Notification.show("AI fehlgeschlagen: " + error.getMessage())
                                .addThemeVariants(NotificationVariant.ERROR);
                    } else {
                        log.info("Form filler: {} tool call(s)", result.toolCalls().size());
                        Notification.show(result.summary() == null || result.summary().isBlank()
                                ? "Formular ausgefüllt. Bitte prüfen und hinzufügen." : result.summary());
                    }
                }));
    }

    private void setBusy(boolean busy) {
        fill.setEnabled(!busy);
        fill.setText(busy ? "Füllt aus…" : "Formular ausfüllen");
    }

    /** What the AI may touch while it fills in the form; for the tests. */
    AgentScope scope() {
        return scope;
    }
}
