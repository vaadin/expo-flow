package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceCategory;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.ai.script.DraivScriptTools;
import com.vaadin.draiv.script.Script;
import com.vaadin.draiv.script.ScriptRunner;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.script.Step;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                { "op": "navigate", "to": "ai/invoices" },
                { "op": "click", "id": "invoice-new-button" },
                { "op": "set", "id": "invoice-number", "value": "TEST-001" },
                { "op": "set", "id": "invoice-supplier", "value": "Testfirma GmbH" },
                { "op": "set", "id": "invoice-date", "value": "2026-11-02" },
                { "op": "set", "id": "invoice-due-date", "value": "2026-12-02" },
                { "op": "set", "id": "invoice-net", "value": "100" },
                { "op": "set", "id": "invoice-vat-rate", "value": "19 %" },
                { "op": "set", "id": "invoice-category", "value": "Büro" },
                { "op": "click", "id": "invoice-save-button" },
                { "op": "click", "id": "invoice-new-button" },
                { "op": "set", "id": "invoice-number", "value": "TEST-002" },
                { "op": "set", "id": "invoice-supplier", "value": "Testfirma GmbH" },
                { "op": "set", "id": "invoice-date", "value": "2026-11-02" },
                { "op": "set", "id": "invoice-due-date", "value": "2026-12-02" },
                { "op": "set", "id": "invoice-net", "value": "200" },
                { "op": "set", "id": "invoice-vat-rate", "value": "7 %" },
                { "op": "set", "id": "invoice-category", "value": "Reise" },
                { "op": "click", "id": "invoice-save-button" },
                { "op": "click", "id": "invoice-new-button" },
                { "op": "set", "id": "invoice-number", "value": "TEST-003" },
                { "op": "set", "id": "invoice-supplier", "value": "Testfirma GmbH" },
                { "op": "set", "id": "invoice-date", "value": "2026-11-02" },
                { "op": "set", "id": "invoice-due-date", "value": "2026-12-02" },
                { "op": "set", "id": "invoice-net", "value": "300" },
                { "op": "set", "id": "invoice-vat-rate", "value": "0 %" },
                { "op": "set", "id": "invoice-category", "value": "Software" },
                { "op": "click", "id": "invoice-save-button" }
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
        // The tab lists it right away
        assertEquals("drei-testrechnungen",
                ((Script) macroGrid().getListDataView().getItems().findFirst().orElseThrow()).name());
        assertEquals("ok", scriptTools.runScript("drei-testrechnungen", null).status());

        assertEquals(before + 3, invoices.count());
        assertTrue(getCurrentView() instanceof AiInvoiceCrudView);
        var gross = invoices.findAll().stream().filter(invoice -> invoice.getInvoiceNumber().startsWith("TEST-"))
                .map(Invoice::getGross).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("633.00"), gross); // 119 + 214 + 300

        // Back on the tab, a person replays it: no model involved
        navigate(AiMacroView.class);
        var actions = (HorizontalLayout) test(macroGrid()).getCellComponent(0, 3);
        test((Button) actions.getComponentAt(0)).click();

        assertEquals(before + 6, invoices.count());
    }

    /**
     * "Erstelle ein Makro, das die Kategorie einer Rechnung ändert; Rechnungsnummer und neue Kategorie als
     * Eingabe": what the AI saves, run with inputs and no model.
     */
    @Test
    void aMacroWithInputsChangesTheCategoryOfAnInvoice() {
        scriptTools.saveScript("kategorie-aendern", """
                {
                  "name": "kategorie-aendern",
                  "description": "Ändert die Kategorie einer Rechnung.",
                  "inputs": [
                    { "name": "rechnungsnummer", "type": "string" },
                    { "name": "kategorie", "type": "select",
                      "choices": ["Hardware", "Software", "Reise", "Marketing", "Büro"] }
                  ],
                  "steps": [
                    { "op": "navigate", "to": "ai/invoices" },
                    { "op": "set", "id": "invoice-search", "value": "${rechnungsnummer}" },
                    { "op": "apply", "id": "invoice-crud-grid", "capability": "selectable",
                      "operation": "selectByContentMatching", "args": ["${rechnungsnummer}"] },
                    { "op": "set", "id": "invoice-category", "value": "${kategorie}" },
                    { "op": "click", "id": "invoice-save-button" }
                  ]
                }
                """);
        long before = invoices.count();

        ScriptRunner.run(scripts.load("kategorie-aendern"),
                Map.of("rechnungsnummer", "RE-2026-102", "kategorie", "Marketing"), UI.getCurrent());

        assertEquals(InvoiceCategory.MARKETING, invoice("RE-2026-102").getCategory());
        assertEquals(before, invoices.count()); // changed, not added
        scripts.delete("kategorie-aendern");
    }

    /** Status and delete are for people: a macro cannot reach them, the data stays as it is. */
    @Test
    void aMacroCannotChangeTheStatusOrDelete() {
        var invoice = invoice("RE-2026-103");
        var selectRow = new Script("zeile-waehlen", "Wählt die Rechnung aus.", List.of(), List.of(
                new Step.Navigate("ai/invoices"),
                new Step.Set("invoice-search", null, "RE-2026-103"),
                new Step.Apply("invoice-crud-grid", null, "selectable", "selectByContentMatching",
                        List.of("RE-2026-103"), null)));
        ScriptRunner.run(selectRow, Map.of(), UI.getCurrent());

        for (var forbidden : List.<Step>of(new Step.Set("invoice-status", null, "Bezahlt"),
                new Step.Click("invoice-delete-button", null))) {
            assertThrows(RuntimeException.class, () -> ScriptRunner.run(
                    new Script("verboten", "", List.of(), List.of(forbidden)), Map.of(), UI.getCurrent()));
        }

        assertEquals(invoice.getStatus(), invoice("RE-2026-103").getStatus());
        assertTrue(invoices.existsById(invoice.getId()));
    }

    private Invoice invoice(String number) {
        return invoices.findAll().stream().filter(invoice -> invoice.getInvoiceNumber().equals(number))
                .findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private Grid<Object> macroGrid() {
        return find(Grid.class).id("macro-grid");
    }
}
