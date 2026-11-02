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
                InvoiceCategory.HARDWARE, "Drei Monitore");
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
        var draft = new InvoiceDraft(null, " ", null, null, new BigDecimal("10"), 16, null, null, null);

        assertEquals(6, draft.problems().size(), draft.problems()::toString);
        assertTrue(draft.problems().contains("Unbekannter MwSt-Satz: 16 %."));
    }
}
