package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.draiv.ai.mcp.AliasRegistry;
import com.vaadin.draiv.ai.mcp.CapabilityMcpTools;
import com.vaadin.draiv.ai.mcp.CatalogAccess;
import com.vaadin.draiv.session.DemoPrincipal;
import com.vaadin.draiv.session.DemoPrincipalUiResolver;
import com.vaadin.draiv.session.UiRegistry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.messages.MessageInput;
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
 * Drives the AI Driven UI area through draiv's tools exactly as the chat's LLM does — {@code navigate},
 * {@code inspect}, then {@code apply} — just without a model in the loop.
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
    void everyTabConstructs() {
        for (var view : List.of(AiGridView.class, AiFormFillerView.class, AiInvoiceUploadView.class,
                AiDashboardView.class, AiMacroView.class, AiGuardrailsView.class)) {
            assertEquals(view, navigate(view).getClass());
        }
    }

    @Test
    void aiNavigatesToTheUploadAndFillsInTheSupplier() {
        navigate(AiGridView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        // "Bring mich zum Rechnungs-Upload"
        var navigated = tools.navigate("ai/upload");
        assertEquals("ok", navigated.status(), navigated::toString);
        assertTrue(getCurrentView() instanceof AiInvoiceUploadView);

        // "Trag als Lieferant Acme GmbH ein"
        String snapshot = tools.inspect(null, null, null);
        apply(snapshot, "invoice-supplier", "settable", "set", "Acme GmbH");

        assertEquals("Acme GmbH", find(TextField.class).id("invoice-supplier").getValue());
    }

    @Test
    void theChatIsOutOfTheAisReach() {
        navigate(AiInvoiceUploadView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());

        String snapshot = tools.inspect(null, null, null);

        assertTrue(snapshot.contains("invoice-supplier"), snapshot);
        assertFalse(snapshot.contains("MessageInput") || snapshot.contains("AI-Assistent"), snapshot);
        assertEquals(1, find(MessageInput.class).all().size()); // it is there, just not for the AI
    }

    @Test
    void aiCanEnterAWholeInvoice() {
        navigate(AiInvoiceUploadView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());
        long before = invoices.count();

        String snapshot = tools.inspect(null, null, null);
        apply(snapshot, "invoice-number", "settable", "set", "RE-2026-114");
        apply(snapshot, "invoice-supplier", "settable", "set", "Acme GmbH");
        apply(snapshot, "invoice-date", "settable", "set", "2026-11-02");
        apply(snapshot, "invoice-due-date", "settable", "set", "2026-11-30");
        apply(snapshot, "invoice-net", "settable", "set", "1000");
        apply(snapshot, "invoice-vat-rate", "settable", "set", "19 %");
        apply(snapshot, "invoice-category", "settable", "set", "Hardware");
        apply(snapshot, "invoice-add-button", "activatable", "activate");

        assertEquals(before + 1, invoices.count());
        Invoice added = latestInvoices().getFirst();
        assertEquals("RE-2026-114", added.getInvoiceNumber());
        assertEquals(new BigDecimal("1190.00"), added.getGross());
    }

    private void apply(String snapshot, String id, String capability, String operation, String... args) {
        var result = tools.apply(selectorFor(snapshot, id), capability, operation, List.of(args), null);
        assertEquals("ok", result.status(), () -> id + ": " + result);
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
    private List<Invoice> latestInvoices() {
        Grid<Invoice> grid = find(Grid.class).id("latest-invoices");
        return grid.getListDataView().getItems().toList();
    }
}
