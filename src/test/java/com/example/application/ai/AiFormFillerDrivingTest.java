package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceCategory;
import com.example.application.ai.invoice.InvoiceRepository;
import com.example.application.ai.invoice.InvoiceService;
import com.example.application.ai.invoice.InvoiceStatus;
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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the Form Filler tab as its AI run does: with the view's {@link com.vaadin.draiv.ai.ui.AgentScope}
 * in effect, {@code inspect} once, then one {@code apply_many}. No model in the loop.
 */
@SpringBootTest
class AiFormFillerDrivingTest extends SpringBrowserlessTest {

    private final UiRegistry registry = new UiRegistry();
    private final CapabilityMcpTools tools = new CapabilityMcpTools(
            new CatalogAccess(new DemoPrincipalUiResolver(registry), new AliasRegistry()));

    @Autowired
    private InvoiceRepository invoices;
    @Autowired
    private InvoiceService invoiceService;

    private Registration scope;

    @BeforeEach
    void openTheFormFillerInItsScope() {
        invoiceService.resetAndSeed();
        var view = navigate(AiFormFillerView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());
        scope = view.scope().applyTo(UI.getCurrent());
    }

    @AfterEach
    void liftScope() {
        scope.remove();
    }

    @Test
    void theAiSeesTheFieldsButNotStatusSaveOrChat() {
        String snapshot = tools.inspect(null, null, null);

        assertTrue(snapshot.contains("invoice-supplier"), snapshot);
        assertTrue(snapshot.contains("invoice-net"), snapshot);
        assertFalse(snapshot.contains("invoice-add-button"), snapshot);
        assertFalse(snapshot.contains("invoice-status"), snapshot);
        assertFalse(snapshot.contains("form-filler-text"), snapshot);
        assertFalse(snapshot.contains("AI-Assistent"), snapshot);
        assertEquals("error", tools.navigate("ai/grid").status());
    }

    @Test
    void theExampleMailFillsTheFormAndAPersonSavesIt() {
        String snapshot = tools.inspect(null, null, null);

        // What the LLM sends for AiFormFillerView.EXAMPLE
        var results = tools.applyMany(List.of(
                set(snapshot, "invoice-number", "RE-2026-114"),
                set(snapshot, "invoice-supplier", "Acme GmbH"),
                set(snapshot, "invoice-date", "2026-10-28"),
                set(snapshot, "invoice-due-date", "2026-11-30"),
                set(snapshot, "invoice-net", "1000"),
                set(snapshot, "invoice-vat-rate", "19 %"),
                set(snapshot, "invoice-category", "Hardware")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        // The person checks and clicks Add
        long before = invoices.count();
        test(find(Button.class).id("invoice-add-button")).click();

        assertEquals(before + 1, invoices.count());
        var added = invoices.findAll().stream()
                .max(Comparator.comparing(invoice -> invoice.getCreatedAt())).orElseThrow();
        assertEquals("RE-2026-114", added.getInvoiceNumber());
        assertEquals(LocalDate.of(2026, 11, 30), added.getDueDate());
        assertEquals(new BigDecimal("1190.00"), added.getGross());
        assertEquals(InvoiceStatus.OPEN, added.getStatus());
    }

    @Test
    void aGermanCategoryLabelCanBeSet() {
        String snapshot = tools.inspect(null, null, null);

        var result = tools.apply(selectorFor(snapshot, "invoice-category"), "settable", "set", List.of("Büro"), null);

        assertEquals("ok", result.status(), result::toString);
        assertEquals(InvoiceCategory.OFFICE, find(Select.class).id("invoice-category").getValue());
    }

    /**
     * draiv sets a ComboBox only to one of its items: allowCustomValue does not let the AI type a new
     * supplier. The goal therefore tells the AI to leave the field empty and say so. When this test
     * fails, draiv has learned custom values: let the AI type new suppliers again.
     */
    @Test
    void aNewSupplierIsNotAnOptionForTheAi() {
        String snapshot = tools.inspect(null, null, null);

        var result = tools.apply(selectorFor(snapshot, "invoice-supplier"), "settable", "set",
                List.of("Neue Firma GmbH"), null);

        assertEquals("error", result.status(), result::toString);
        assertEquals(null, find(ComboBox.class).id("invoice-supplier").getValue());
    }

    private static ApplyStep set(String snapshot, String id, String value) {
        return new ApplyStep(selectorFor(snapshot, id), "settable", "set", List.of(value), null);
    }

    /** The {@code sel=#n} handle that {@code inspect} printed for the component with this id. */
    private static String selectorFor(String snapshot, String id) {
        return snapshot.lines()
                .filter(line -> line.contains("id=\"" + id + "\"") && line.contains("sel="))
                .map(line -> line.substring(line.indexOf("sel=") + "sel=".length()).split("\\s")[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("No " + id + " in snapshot:\n" + snapshot));
    }
}
