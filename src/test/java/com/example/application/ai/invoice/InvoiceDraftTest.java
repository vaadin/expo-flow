package com.example.application.ai.invoice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceDraftTest {

    private static InvoiceDraft draft(String net, Integer rate, String gross) {
        return new InvoiceDraft("RE-2026-114", "Acme GmbH", LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 30),
                net == null ? null : new BigDecimal(net), rate, gross == null ? null : new BigDecimal(gross),
                InvoiceCategory.HARDWARE, "Drei Monitore", List.of(
                        new FieldEvidence("dueDate", Certainty.MEDIUM, "Zahlbar innerhalb 30 Tagen"),
                        new FieldEvidence("net", Certainty.HIGH, "Nettobetrag 1.000,00 €"),
                        new FieldEvidence("gross", Certainty.HIGH, "Rechnungsbetrag 1.190,00 €"),
                        new FieldEvidence("category", Certainty.LOW, "3x Monitor 27 Zoll")));
    }

    @Test
    void aConsistentInvoiceHasNoProblems() {
        assertEquals(List.of(), draft("1000.00", 19, "1190.00").problems());
    }

    @Test
    void netPlusVatThatDoesNotMatchTheGrossIsReported() {
        var problems = draft("1000.00", 19, "1200.00").problems();

        assertEquals(1, problems.size());
        assertTrue(problems.getFirst().contains("1.190,00"), problems::toString);
        assertTrue(problems.getFirst().contains("1.200,00"), problems::toString);
    }

    @Test
    void netIsComputedFromTheGrossWhenOnlyThatWasRead() {
        var draft = draft(null, 7, "107.00").withNetFromGross();

        assertEquals(new BigDecimal("100.00"), draft.net());
        assertTrue(draft.problems().isEmpty(), draft.problems()::toString);
    }

    @Test
    void anUnknownVatRateAndMissingFieldsAreReported() {
        var draft = new InvoiceDraft(null, " ", null, null, new BigDecimal("10"), 16, null, null, null, null);

        assertEquals(6, draft.problems().size(), draft.problems()::toString);
        assertTrue(draft.problems().contains("Unbekannter MwSt-Satz: 16 %."));
    }

    @Test
    void theModelsCertaintyAndSourceArePassedOn() {
        var evidence = draft("1000.00", 19, "1190.00").evidenceFor("dueDate").orElseThrow();

        assertEquals(Certainty.MEDIUM, evidence.certainty());
        assertEquals("Zahlbar innerhalb 30 Tagen", evidence.source());
        assertEquals(List.of("category"), draft("1000.00", 19, "1190.00").uncertainFields());
    }

    @Test
    void whatThePlausibilityCheckContradictsIsUncertain() {
        var draft = draft("1000.00", 19, "1200.00");

        assertEquals(Certainty.LOW, draft.evidenceFor("net").orElseThrow().certainty()); // the model said HIGH
        assertEquals(List.of("net", "vatRatePercent", "category"), draft.uncertainFields());
    }

    @Test
    void aValueWithoutEvidenceIsMediumAndAMissingValueHasNone() {
        var draft = draft("1000.00", 19, "1190.00");

        assertEquals(Certainty.MEDIUM, draft.evidenceFor("supplier").orElseThrow().certainty());
        assertTrue(draft.evidenceFor("supplier").orElseThrow().source() == null);
        assertTrue(draft(null, 19, null).evidenceFor("net").isEmpty());
    }

    @Test
    void aNetComputedFromTheGrossIsMediumAndPointsAtTheGross() {
        var evidence = draft(null, 19, "1190.00").withNetFromGross().evidenceFor("net").orElseThrow();

        assertEquals(Certainty.MEDIUM, evidence.certainty());
        assertEquals("Rechnungsbetrag 1.190,00 €", evidence.source());
    }
}
