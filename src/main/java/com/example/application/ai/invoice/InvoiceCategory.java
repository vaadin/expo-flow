package com.example.application.ai.invoice;

public enum InvoiceCategory {
    HARDWARE("Hardware"), SOFTWARE("Software"), TRAVEL("Reise"), MARKETING("Marketing"), OFFICE("Büro");

    private final String label;

    InvoiceCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
