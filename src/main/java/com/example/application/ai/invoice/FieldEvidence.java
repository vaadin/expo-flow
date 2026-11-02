package com.example.application.ai.invoice;

/**
 * Where a value in an {@link InvoiceDraft} comes from and how sure the extraction is about it.
 *
 * @param field     the draft field: invoiceNumber, supplier, invoiceDate, dueDate, net, vatRatePercent or
 *                  category
 * @param certainty how sure the value is
 * @param source    the text on the receipt the value was read from, quoted verbatim; for a derived value
 *                  the text it was derived from
 */
public record FieldEvidence(String field, Certainty certainty, String source) {
}
