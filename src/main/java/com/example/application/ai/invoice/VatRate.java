package com.example.application.ai.invoice;

import java.math.BigDecimal;

/** The German VAT rates. */
public enum VatRate {
    ZERO(0), REDUCED(7), STANDARD(19);

    private final int percent;

    VatRate(int percent) {
        this.percent = percent;
    }

    public int percent() {
        return percent;
    }

    public BigDecimal factor() {
        return BigDecimal.valueOf(percent).movePointLeft(2);
    }

    /** "19 %", the label the form shows and the AI picks. */
    public String label() {
        return percent + " %";
    }
}
