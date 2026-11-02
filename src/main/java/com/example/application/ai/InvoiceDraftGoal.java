package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceDraft;

/**
 * The goal that has the AI enter an {@link InvoiceDraft} into the invoice form on screen, field by field,
 * so the person sees each value arrive (the fields flash) and the same validation applies as for typing.
 */
final class InvoiceDraftGoal {

    private InvoiceDraftGoal() {
    }

    static String fillForm(InvoiceDraft draft, String fileName) {
        return """
                Enter this invoice in the invoice form on screen. It was read from the uploaded file "%s":
                - Rechnungsnummer (invoice-number): %s
                - Lieferant (invoice-supplier): %s
                - Kontakt-E-Mail (invoice-contact-email): %s
                - Rechnungsdatum (invoice-date): %s
                - Fällig am (invoice-due-date): %s
                - Netto (invoice-net): %s
                - MwSt-Satz (invoice-vat-rate): %s
                - Kategorie (invoice-category): %s
                - Beleg (invoice-receipt): %s

                Inspect once, then set every field that has a value in a single apply_many, with capability \
                "settable", operation "set"; leave the "none" fields alone. Do not click any button: a person \
                checks the form and adds the invoice. Reply in one short German sentence.
                """.formatted(fileName,
                orNone(draft.invoiceNumber()), orNone(draft.supplier()), orNone(draft.contactEmail()),
                orNone(draft.invoiceDate()), orNone(draft.dueDate()),
                draft.net() == null ? "none" : draft.net().toPlainString(),
                draft.vatRate() == null ? "none" : "\"" + draft.vatRate().label() + "\"",
                draft.category() == null ? "none" : "\"" + draft.category().label() + "\"",
                fileName);
    }

    private static String orNone(Object value) {
        return value == null || value.toString().isBlank() ? "none" : value.toString();
    }
}
