package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.example.application.ai.invoice.InvoiceStatus;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.script.Script;
import com.vaadin.draiv.script.ScriptRunner;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every tab's fallback macro still runs against the current views, without a model. */
@SpringBootTest
class FallbackMacrosTest extends SpringBrowserlessTest {

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceRepository invoices;
    @Autowired
    private ScriptStore scripts;
    @Autowired
    private FallbackMacros fallbackMacros;

    @BeforeEach
    void startFromTheDemoData() {
        invoiceService.resetAndSeed();
        navigate(AiMacroView.class);
    }

    @Test
    void theMacroTabListsTheFallbacksAndResetDropsOthers() {
        scripts.save(FallbackMacros.GRID.withName("von-der-ai"));

        fallbackMacros.reset();

        assertEquals(FallbackMacros.ALL.stream().map(Script::name).sorted().toList(), scripts.list());
    }

    @Test
    @SuppressWarnings("unchecked")
    void grid() {
        run(FallbackMacros.GRID);

        Grid<Invoice> grid = find(Grid.class).id("invoice-grid");
        var shown = grid.getListDataView().getItems().toList();
        assertFalse(shown.isEmpty());
        assertTrue(shown.stream().allMatch(invoice -> invoice.getSupplier().equals("Acme GmbH")
                && invoice.getStatus() == InvoiceStatus.OPEN
                && invoice.getGross().compareTo(new BigDecimal("1000")) >= 0));
    }

    @Test
    void formFiller() {
        run(FallbackMacros.FORM_FILLER);

        assertTrue(getCurrentView() instanceof AiFormFillerView);
        assertTrue(find(TextField.class).id("invoice-gross").getValue().startsWith("1.190,00"));
    }

    @Test
    void upload() {
        long before = invoices.count();

        run(FallbackMacros.UPLOAD);

        assertTrue(find(TextField.class).id("invoice-gross").getValue().startsWith("3.184,44")); // as on the PDF
        assertEquals(before, invoices.count()); // a person adds it
    }

    @Test
    void dashboard() {
        run(FallbackMacros.DASHBOARD);

        var view = (AiDashboardView) getCurrentView();
        assertEquals(ChartType.BAR, view.shownType());
    }

    @Test
    void macro() {
        long before = invoices.count();

        run(FallbackMacros.MACRO);

        assertEquals(before + 3, invoices.count());
    }

    @Test
    void guardrailsRefuseTheMacroToo() {
        long paid = paidCount();

        run(FallbackMacros.GUARDRAILS);

        assertTrue(getCurrentView() instanceof AiGuardrailsView);
        assertEquals(paid, paidCount());
    }

    private void run(Script script) {
        ScriptRunner.run(script, Map.of(), UI.getCurrent());
    }

    private long paidCount() {
        return invoices.findAll().stream().filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID).count();
    }
}
