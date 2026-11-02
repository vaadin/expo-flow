package com.example.application.ai.invoice;

import java.util.Comparator;

/** The orders the AI Grid offers, as one choice the AI can set, rather than clicking grid headers. */
public enum InvoiceSort {
    NEWEST("Rechnungsdatum, neueste zuerst", Comparator.comparing(Invoice::getInvoiceDate).reversed()),
    DUE_FIRST("Fälligkeit, früheste zuerst", Comparator.comparing(Invoice::getDueDate)),
    DUE_LAST("Fälligkeit, späteste zuerst", Comparator.comparing(Invoice::getDueDate).reversed()),
    HIGHEST("Betrag, höchster zuerst", Comparator.comparing(Invoice::getGross).reversed()),
    LOWEST("Betrag, niedrigster zuerst", Comparator.comparing(Invoice::getGross)),
    SUPPLIER("Lieferant, A–Z", Comparator.comparing(Invoice::getSupplier, String.CASE_INSENSITIVE_ORDER));

    private final String label;
    private final Comparator<Invoice> comparator;

    InvoiceSort(String label, Comparator<Invoice> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Invoice> comparator() {
        return comparator;
    }
}
