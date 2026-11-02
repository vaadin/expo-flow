package com.example.application.ai.invoice;

import com.example.application.data.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

/** An incoming invoice, the one data type all tabs of the AI Driven UI area work on. */
@Entity
public class Invoice extends AbstractEntity {

    @NotBlank
    private String invoiceNumber;

    @NotBlank
    private String supplier;

    @NotNull
    private LocalDate invoiceDate;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2)
    private BigDecimal net;

    @NotNull
    @Enumerated(EnumType.STRING)
    private VatRate vatRate = VatRate.STANDARD;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InvoiceCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    /** File name of the uploaded receipt, if there is one. */
    private String receipt;

    /** When the invoice was entered, so "the latest invoices" shows a new one on top. */
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /** Net plus VAT, computed rather than stored so it can never disagree with them. */
    @Transient
    public BigDecimal getGross() {
        if (net == null || vatRate == null) {
            return null;
        }
        return net.add(net.multiply(vatRate.factor())).setScale(2, RoundingMode.HALF_UP);
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getNet() {
        return net;
    }

    public void setNet(BigDecimal net) {
        this.net = net;
    }

    public VatRate getVatRate() {
        return vatRate;
    }

    public void setVatRate(VatRate vatRate) {
        this.vatRate = vatRate;
    }

    public InvoiceCategory getCategory() {
        return category;
    }

    public void setCategory(InvoiceCategory category) {
        this.category = category;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }
}
