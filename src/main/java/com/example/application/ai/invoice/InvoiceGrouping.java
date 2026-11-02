package com.example.application.ai.invoice;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

/** How the dashboard adds up the invoices: one bar, slice or point per group. */
public enum InvoiceGrouping {
    CATEGORY("Ausgaben pro Kategorie", invoice -> invoice.getCategory().label(),
            Comparator.comparing(invoice -> invoice.getCategory().ordinal())),
    SUPPLIER("Ausgaben pro Lieferant", Invoice::getSupplier,
            Comparator.comparing(Invoice::getSupplier, String.CASE_INSENSITIVE_ORDER)),
    MONTH("Ausgaben pro Monat", invoice -> invoice.getInvoiceDate().format(Formats.MONTH),
            Comparator.comparing(Invoice::getInvoiceDate));

    private final String label;
    private final Function<Invoice, String> group;
    private final Comparator<Invoice> order;

    InvoiceGrouping(String label, Function<Invoice, String> group, Comparator<Invoice> order) {
        this.label = label;
        this.group = group;
        this.order = order;
    }

    public String label() {
        return label;
    }

    /** The group an invoice belongs to, as shown on the chart axis. */
    public String groupOf(Invoice invoice) {
        return group.apply(invoice);
    }

    /** Puts invoices in the order their groups appear on the axis: categories, A–Z, by month. */
    Comparator<Invoice> order() {
        return order;
    }

    private static final class Formats {
        static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM yyyy", Locale.GERMANY);
    }
}
