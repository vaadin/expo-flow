package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceCategory;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.ai.mcp.AliasRegistry;
import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.mcp.CapabilityMcpTools.ApplyStep;
import com.vaadin.draiv.ai.mcp.CatalogAccess;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.draiv.session.DemoPrincipalUiResolver;
import com.vaadin.draiv.session.UiRegistry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The invoices tab (CRUD) and the area around it, driven through draiv's tools exactly as the chat's LLM
 * does — {@code navigate}, {@code inspect}, then {@code apply} — just without a model in the loop.
 */
@SpringBootTest
class AiInvoiceDrivingTest extends SpringBrowserlessTest {

    private final UiRegistry registry = new UiRegistry();
    private final CapabilityMcpTools tools = new CapabilityMcpTools(
            new CatalogAccess(new DemoPrincipalUiResolver(registry), new AliasRegistry()));

    @Autowired
    private InvoiceRepository invoices;
    @Autowired
    private InvoiceService invoiceService;

    @BeforeEach
    void resetInvoices() {
        invoiceService.resetAndSeed();
    }

    @Test
    void demoInvoicesAreSeeded() {
        assertEquals(50, invoices.count());
        assertTrue(invoices.findAll().stream().anyMatch(invoice -> invoice.getSupplier().equals("Acme GmbH")));
    }

    @Test
    void demoInvoicesHaveAContactEmail() {
        assertTrue(invoices.findAll().stream().allMatch(invoice -> invoice.getContactEmail().endsWith(".example")));
    }

    @Test
    void everyTabConstructs() {
        for (var view : List.of(AiGridView.class, AiFormFillerView.class, AiInvoiceCrudView.class,
                AiDashboardView.class, AiMacroView.class, AiGuardrailsView.class)) {
            assertEquals(view, navigate(view).getClass());
        }
    }

    @Test
    void aiNavigatesToTheFormAndFillsInTheSupplier() {
        navigate(AiGridView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        // "Bring mich zum Rechnungs-Upload"
        var navigated = tools.navigate("ai/form");
        assertEquals("ok", navigated.status(), navigated::toString);
        assertTrue(getCurrentView() instanceof AiFormFillerView);

        // "Trag als Lieferant Acme GmbH ein"
        String snapshot = tools.inspect(null, null, null);
        apply(snapshot, "invoice-supplier", "settable", "set", "Acme GmbH");

        assertEquals("Acme GmbH", find(TextField.class).id("invoice-supplier").getValue());
    }

    @Test
    void theAiSeesNeitherTheChatNorStatusAndDelete() {
        navigate(AiInvoiceCrudView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        String snapshot = tools.inspect(null, null, null);

        assertTrue(snapshot.contains("invoice-crud-grid") && snapshot.contains("invoice-save-button"), snapshot);
        assertFalse(snapshot.contains("invoice-delete-button"), snapshot);
        assertFalse(snapshot.contains("invoice-status"), snapshot);
        assertFalse(snapshot.contains("MessageInput") || snapshot.contains("AI-Assistent"), snapshot);
        assertEquals(1, find(MessageInput.class).all().size()); // it is there, just not for the AI
    }

    /** "Ändere bei RE-2026-101 die Kategorie auf Büro": search, select the row, set, save. */
    @Test
    void theAiEditsAnInvoice() {
        navigate(AiInvoiceCrudView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        String snapshot = tools.inspect(null, null, null);
        var results = tools.applyMany(List.of(
                step(snapshot, "invoice-search", "settable", "set", "RE-2026-101"),
                step(snapshot, "invoice-crud-grid", "selectable", "selectByContentMatching", "RE-2026-101")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);
        assertEquals("RE-2026-101", find(TextField.class).id("invoice-number").getValue());

        snapshot = tools.inspect(null, null, null);
        results = tools.applyMany(List.of(
                step(snapshot, "invoice-category", "settable", "set", "Büro"),
                step(snapshot, "invoice-contact-email", "settable", "set", "neu@acme.example"),
                step(snapshot, "invoice-save-button", "activatable", "activate")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        var edited = invoices.findAll().stream()
                .filter(invoice -> invoice.getInvoiceNumber().equals("RE-2026-101")).findFirst().orElseThrow();
        assertEquals(InvoiceCategory.OFFICE, edited.getCategory());
        assertEquals("neu@acme.example", edited.getContactEmail());
        assertEquals(50, invoices.count()); // changed, not added
    }

    @Test
    void theAiCreatesAnInvoice() {
        navigate(AiInvoiceCrudView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        String snapshot = tools.inspect(null, null, null);
        var results = tools.applyMany(List.of(
                step(snapshot, "invoice-new-button", "activatable", "activate"),
                step(snapshot, "invoice-number", "settable", "set", "RE-2026-200"),
                step(snapshot, "invoice-supplier", "settable", "set", "Neue Firma GmbH"),
                step(snapshot, "invoice-date", "settable", "set", "2026-10-30"),
                step(snapshot, "invoice-due-date", "settable", "set", "2026-11-13"),
                step(snapshot, "invoice-net", "settable", "set", "250.00"),
                step(snapshot, "invoice-vat-rate", "settable", "set", "7 %"),
                step(snapshot, "invoice-category", "settable", "set", "Reise"),
                step(snapshot, "invoice-save-button", "activatable", "activate")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        assertEquals(51, invoices.count());
        var added = shownInvoices().stream().filter(invoice -> invoice.getInvoiceNumber().equals("RE-2026-200"))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("267.50"), added.getGross());
    }

    @Test
    void aPersonDeletesAnInvoiceAfterConfirming() {
        navigate(AiInvoiceCrudView.class);
        var invoice = shownInvoices().getFirst();
        crudGrid().asSingleSelect().setValue(invoice);

        test(find(Button.class).id("invoice-delete-button")).click();
        test(find(ConfirmDialog.class).single()).confirm();

        assertEquals(49, invoices.count());
        assertFalse(invoices.existsById(invoice.getId()));
    }

    private void apply(String snapshot, String id, String capability, String operation, String... args) {
        var result = tools.apply(selectorFor(snapshot, id), capability, operation, List.of(args), null);
        assertEquals("ok", result.status(), () -> id + ": " + result);
    }

    private static ApplyStep step(String snapshot, String id, String capability, String operation, String... args) {
        return new ApplyStep(selectorFor(snapshot, id), capability, operation, List.of(args), null);
    }

    /** The {@code sel=#n} handle that {@code inspect} printed for the component with this id. */
    private static String selectorFor(String snapshot, String id) {
        return snapshot.lines()
                .filter(line -> line.contains("id=\"" + id + "\"") && line.contains("sel="))
                .map(line -> line.substring(line.indexOf("sel=") + "sel=".length()).split("\\s")[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("No " + id + " in snapshot:\n" + snapshot));
    }

    @SuppressWarnings("unchecked")
    private Grid<Invoice> crudGrid() {
        return find(Grid.class).id("invoice-crud-grid");
    }

    private List<Invoice> shownInvoices() {
        return crudGrid().getListDataView().getItems().toList();
    }
}
