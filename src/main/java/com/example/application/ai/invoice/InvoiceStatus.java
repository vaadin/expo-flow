package com.example.application.ai.invoice;

public enum InvoiceStatus {
    OPEN("Offen"), PAID("Bezahlt"), OVERDUE("Überfällig");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
