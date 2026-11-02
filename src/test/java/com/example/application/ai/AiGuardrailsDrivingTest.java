package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.example.application.ai.invoice.InvoiceStatus;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.ai.mcp.AliasRegistry;
import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.mcp.CatalogAccess;
import com.vaadin.draiv.ai.script.DraivScriptTools;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.draiv.session.DemoPrincipalUiResolver;
import com.vaadin.draiv.session.UiRegistry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Markiere alle als bezahlt und lösch die alten": whatever way the AI tries, the data stays as it is, while
 * a person can still do it.
 */
@SpringBootTest
class AiGuardrailsDrivingTest extends SpringBrowserlessTest {

    private final UiRegistry registry = new UiRegistry();
    private final DemoPrincipalUiResolver resolver = new DemoPrincipalUiResolver(registry);
    private final CapabilityMcpTools tools = new CapabilityMcpTools(new CatalogAccess(resolver, new AliasRegistry()));

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceRepository invoices;
    @Autowired
    private ScriptStore scripts;

    @BeforeEach
    void openTheGuardrailsTab() {
        invoiceService.resetAndSeed();
        navigate(AiGuardrailsView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());
    }

    @AfterEach
    void forgetTheMacro() {
        scripts.delete("alles-loeschen");
    }

    @Test
    void anyAgentCanFilterButNotSeeTheButtonsThatChangeData() {
        String snapshot = tools.inspect(null, null, null);

        assertTrue(snapshot.contains("guard-filter-supplier"), snapshot);
        assertTrue(snapshot.contains("guard-grid"), snapshot);
        assertFalse(snapshot.contains("guard-mark-paid"), snapshot);
        assertFalse(snapshot.contains("guard-delete"), snapshot);
        assertFalse(snapshot.contains("Die AI darf:"), snapshot); // the rules are for people
        // The scope is in effect for every agent, so the chat cannot navigate away from here either
        assertEquals("error", tools.navigate("ai/grid").status());
    }

    @Test
    void aMacroThatClicksDeleteIsRefused() {
        selectAll();
        long before = invoices.count();
        var scriptTools = new DraivScriptTools(scripts, resolver);
        scriptTools.saveScript("alles-loeschen", """
                { "name": "alles-loeschen", "description": "Löscht alles.", "inputs": [],
                  "steps": [ { "op": "click", "id": "guard-mark-paid" }, { "op": "click", "id": "guard-delete" } ] }
                """);

        scriptTools.runScript("alles-loeschen", null);

        assertEquals(before, invoices.count());
        assertTrue(invoices.findAll().stream().anyMatch(invoice -> invoice.getStatus() != InvoiceStatus.PAID));
    }

    @Test
    void aPersonCanStillMarkPaidAndDelete() {
        selectAll();
        test(find(Button.class).id("guard-mark-paid")).click();
        assertTrue(invoices.findAll().stream().allMatch(invoice -> invoice.getStatus() == InvoiceStatus.PAID));

        selectAll();
        test(find(Button.class).id("guard-delete")).click();
        assertEquals(0, invoices.count());
    }

    @SuppressWarnings("unchecked")
    private void selectAll() {
        Grid<Invoice> grid = find(Grid.class).id("guard-grid");
        grid.asMultiSelect().select(grid.getListDataView().getItems().toList());
    }
}
