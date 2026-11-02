package com.example.application.ai.invoice;

/** What {@link InvoiceService} broadcasts to open views: an invoice was created, changed or deleted. */
public record InvoiceChange(Kind kind, Invoice invoice) {

    public enum Kind {
        CREATED, UPDATED, DELETED
    }
}
