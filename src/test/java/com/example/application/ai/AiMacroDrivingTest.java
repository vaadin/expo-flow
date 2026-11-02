package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.ai.script.DraivScriptTools;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.draiv.session.DemoPrincipalUiResolver;
import com.vaadin.draiv.session.UiRegistry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The macro tab without a model: save and run a script through draiv's script tools as the AI does, then
 * replay it with the tab's own button.
 */
@SpringBootTest
class AiMacroDrivingTest extends SpringBrowserlessTest {

    /** What the AI saves for "Lege drei Testrechnungen an und speichere das als Makro". */
    private static final String THREE_TEST_INVOICES = """
            {
              "name": "drei-testrechnungen",
              "description": "Legt drei Testrechnungen an.",
              "inputs": [],
              "steps": [
                { "op": "navigate", "to": "ai/upload" },
                { "op": "set", "id": "invoice-number", "value": "TEST-001" },
                { "op": "set", "id": "invoice-supplier", "value": "Testfirma GmbH" },
                { "op": "set", "id": "invoice-date", "value": "2026-11-02" },
                { "op": "set", "id": "invoice-due-date", "value": "2026-12-02" },
                { "op": "set", "id": "invoice-net", "value": "100" },
                { "op": "set", "id": "invoice-vat-rate", "value": "19 %" },
                { "op": "set", "id": "invoice-category", "value": "Büro" },
                { "op": "click", "id": "invoice-add-button" },
                { "op": "set", "id": "invoice-number", "value": "TEST-002" },
                { "op": "set", "id": "invoice-supplier", "value": "Testfirma GmbH" },
                { "op": "set", "id": "invoice-date", "value": "2026-11-02" },
                { "op": "set", "id": "invoice-due-date", "value": "2026-12-02" },
                { "op": "set", "id": "invoice-net", "value": "200" },
                { "op": "set", "id": "invoice-vat-rate", "value": "7 %" },
                { "op": "set", "id": "invoice-category", "value": "Reise" },
                { "op": "click", "id": "invoice-add-button" },
                { "op": "set", "id": "invoice-number", "value": "TEST-003" },
                { "op": "set", "id": "invoice-supplier", "value": "Testfirma GmbH" },
                { "op": "set", "id": "invoice-date", "value": "2026-11-02" },
                { "op": "set", "id": "invoice-due-date", "value": "2026-12-02" },
                { "op": "set", "id": "invoice-net", "value": "300" },
                { "op": "set", "id": "invoice-vat-rate", "value": "0 %" },
                { "op": "set", "id": "invoice-category", "value": "Software" },
                { "op": "click", "id": "invoice-add-button" }
              ]
            }
            """;

    private final UiRegistry registry = new UiRegistry();

    @Autowired
    private ScriptStore scripts;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceRepository invoices;

    private DraivScriptTools scriptTools;

    @BeforeEach
    void openTheMacroTab() {
        invoiceService.resetAndSeed();
        navigate(AiMacroView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());
        scriptTools = new DraivScriptTools(scripts, new DemoPrincipalUiResolver(registry));
    }

    @AfterEach
    void forgetTheMacro() {
        scripts.delete("drei-testrechnungen");
    }

    @Test
    void theAiSavesAndRunsAMacroThenItRunsWithoutAi() {
        long before = invoices.count();

        // The AI's two tool calls
        scriptTools.saveScript("drei-testrechnungen", THREE_TEST_INVOICES);
        assertEquals(1, macroGrid().getListDataView().getItems().count()); // the tab lists it right away
        assertEquals("ok", scriptTools.runScript("drei-testrechnungen", null).status());

        assertEquals(before + 3, invoices.count());
        assertTrue(getCurrentView() instanceof AiInvoiceUploadView);
        var gross = invoices.findAll().stream().filter(invoice -> invoice.getInvoiceNumber().startsWith("TEST-"))
                .map(Invoice::getGross).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("633.00"), gross); // 119 + 214 + 300

        // Back on the tab, a person replays it: no model involved
        navigate(AiMacroView.class);
        var actions = (HorizontalLayout) test(macroGrid()).getCellComponent(0, 3);
        test((Button) actions.getComponentAt(0)).click();

        assertEquals(before + 6, invoices.count());
    }

    @SuppressWarnings("unchecked")
    private Grid<Object> macroGrid() {
        return find(Grid.class).id("macro-grid");
    }
}
