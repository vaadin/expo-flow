package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceForm;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Tab 2: paste a supplier's e-mail, and the AI fills in the invoice form from it. The AI may only
 * touch the form's fields ({@link AgentScope}): the status and the Add button are out of its reach,
 * so a person checks the result and saves it.
 */
@PageTitle("Form Filler")
@Route("ai/form")
public class AiFormFillerView extends AiTabView {

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
            - invoice-supplier (Lieferant): the company name as written, with its legal form (GmbH, AG…)
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

    private final AgentScope scope;

    public AiFormFillerView(UiAssistant assistant, InvoiceService invoiceService) {
        super("Form Filler",
                "Eine Lieferanten-E-Mail einfügen: Die AI füllt das Formular aus, speichern tut der Mensch.");
        var form = new InvoiceForm(invoiceService);
        // The AI sees the form's fields, but neither the status nor the button that saves
        this.scope = AgentScope.within(form).excluding(form.getAddButton(), form.getStatusField());

        var prompt = AiPromptBar.multiLine(assistant, scope, GOAL::formatted, "Formular ausfüllen");
        prompt.getPrompt().setLabel("Lieferanten-E-Mail");
        prompt.getPrompt().setPlaceholder("E-Mail oder Rechnungsdaten hier einfügen…");
        prompt.getPrompt().setId("form-filler-text");
        prompt.addFallback(FallbackMacros.FORM_FILLER);
        var example = new Button("Beispiel-E-Mail", e -> prompt.getPrompt().setValue(EXAMPLE));
        example.addThemeVariants(ButtonVariant.TERTIARY);
        example.getElement().setAttribute("data-draiv-control", "true");

        add(prompt, example, form);
    }

    /** What the AI may touch while it fills in the form; for the tests. */
    AgentScope scope() {
        return scope;
    }
}
