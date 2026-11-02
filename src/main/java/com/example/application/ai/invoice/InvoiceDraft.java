package com.example.application.ai.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * What the model read off an uploaded invoice. Any field may be missing ({@code null}); {@link #problems()}
 * says what a person should look at before the invoice is added.
 *
 * @param vatRatePercent 0, 7 or 19 for a German invoice; anything else is reported as a problem
 * @param gross          the total payable as printed, to check net plus VAT against
 * @param description    what was bought, in a few words
 * @param evidence       per field, how sure the value is and where on the receipt it comes from; see
 *                       {@link #evidenceFor(String)}
 */
public record InvoiceDraft(String invoiceNumber, String supplier, LocalDate invoiceDate, LocalDate dueDate,
                           BigDecimal net, Integer vatRatePercent, BigDecimal gross,
                           InvoiceCategory category, String description, List<FieldEvidence> evidence) {

    /** The fields {@link #evidenceFor(String)} answers for, in form order. */
    public static final List<String> FIELDS = List.of("invoiceNumber", "supplier", "invoiceDate", "dueDate",
            "net", "vatRatePercent", "category");

    /** A cent of rounding per line adds up; more than this is a real mismatch. */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.05");
    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    /** The VAT rate, or {@code null} when the invoice shows none or one that does not exist here. */
    public VatRate vatRate() {
        return vatRatePercent == null ? null : Arrays.stream(VatRate.values())
                .filter(rate -> rate.percent() == vatRatePercent).findFirst().orElse(null);
    }

    /**
     * Net plus VAT, computed in code rather than trusted from the model. When only the gross amount
     * could be read, net is computed back from it.
     */
    public InvoiceDraft withNetFromGross() {
        if (net != null || gross == null || vatRate() == null) {
            return this;
        }
        var computedNet = gross.divide(BigDecimal.ONE.add(vatRate().factor()), 2, RoundingMode.HALF_UP);
        var grossSource = evidenceFor("gross").map(FieldEvidence::source).orElse(null);
        var evidenceWithNet = new ArrayList<>(evidence == null ? List.<FieldEvidence>of() : evidence);
        evidenceWithNet.removeIf(item -> "net".equals(item.field()));
        evidenceWithNet.add(new FieldEvidence("net", Certainty.MEDIUM, grossSource));
        return new InvoiceDraft(invoiceNumber, supplier, invoiceDate, dueDate, computedNet, vatRatePercent,
                gross, category, description, evidenceWithNet);
    }

    /**
     * How sure the value of {@code field} is and where it comes from, empty when the field has no value. The
     * model's own certainty is lowered where the code knows better: a value the plausibility check
     * contradicts is LOW, a value the model gave without a certainty is MEDIUM.
     */
    public Optional<FieldEvidence> evidenceFor(String field) {
        if (!"gross".equals(field) && valueOf(field) == null) {
            return Optional.empty();
        }
        var given = evidence == null ? null : evidence.stream()
                .filter(item -> item != null && field.equals(item.field())).findFirst().orElse(null);
        var certainty = given == null || given.certainty() == null ? Certainty.MEDIUM : given.certainty();
        if (contradicted(field)) {
            certainty = certainty.atMost(Certainty.LOW);
        }
        var source = given == null || given.source() == null || given.source().isBlank() ? null : given.source();
        return Optional.of(new FieldEvidence(field, certainty, source));
    }

    /** The fields with a value whose certainty is LOW, for a person to look at first. */
    public List<String> uncertainFields() {
        return FIELDS.stream().filter(field -> evidenceFor(field)
                .filter(item -> item.certainty() == Certainty.LOW).isPresent()).toList();
    }

    private Object valueOf(String field) {
        return switch (field) {
            case "invoiceNumber" -> invoiceNumber;
            case "supplier" -> supplier;
            case "invoiceDate" -> invoiceDate;
            case "dueDate" -> dueDate;
            case "net" -> net;
            case "vatRatePercent" -> vatRatePercent;
            case "category" -> category;
            default -> null;
        };
    }

    /** What the plausibility check speaks against: net + VAT not the gross, or a due date before the date. */
    private boolean contradicted(String field) {
        return switch (field) {
            case "net", "vatRatePercent" -> grossMismatch() || ("vatRatePercent".equals(field) && vatRate() == null);
            case "dueDate" -> invoiceDate != null && dueDate != null && dueDate.isBefore(invoiceDate);
            default -> false;
        };
    }

    private boolean grossMismatch() {
        var computed = computedGross();
        return computed != null && gross != null && computed.subtract(gross).abs().compareTo(TOLERANCE) > 0;
    }

    /** Net plus VAT, or {@code null} if either is missing. */
    public BigDecimal computedGross() {
        return net == null || vatRate() == null ? null
                : net.add(net.multiply(vatRate().factor())).setScale(2, RoundingMode.HALF_UP);
    }

    /** What does not add up or is missing, in German for the chat; empty when the draft looks right. */
    public List<String> problems() {
        var problems = new ArrayList<String>();
        if (isBlank(invoiceNumber)) {
            problems.add("Keine Rechnungsnummer gefunden.");
        }
        if (isBlank(supplier)) {
            problems.add("Kein Lieferant gefunden.");
        }
        if (invoiceDate == null) {
            problems.add("Kein Rechnungsdatum gefunden.");
        }
        if (dueDate == null) {
            problems.add("Kein Fälligkeitsdatum gefunden.");
        } else if (invoiceDate != null && dueDate.isBefore(invoiceDate)) {
            problems.add("Fälligkeit liegt vor dem Rechnungsdatum.");
        }
        if (net == null) {
            problems.add("Kein Nettobetrag gefunden.");
        }
        if (vatRatePercent == null) {
            problems.add("Kein MwSt-Satz gefunden.");
        } else if (vatRate() == null) {
            problems.add("Unbekannter MwSt-Satz: " + vatRatePercent + " %.");
        }
        if (grossMismatch()) {
            problems.add("Netto + MwSt ergibt " + EURO.format(computedGross()) + ", auf der Rechnung stehen "
                    + EURO.format(gross) + ".");
        }
        if (category == null) {
            problems.add("Kategorie unklar.");
        }
        return problems;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
