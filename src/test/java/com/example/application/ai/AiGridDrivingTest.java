package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceCategory;
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
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.grid.Grid;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the AI Grid's filter bar as its AI run does: with the view's scope in effect, {@code inspect} once,
 * then one {@code apply_many}. No model in the loop.
 */
@SpringBootTest
class AiGridDrivingTest extends SpringBrowserlessTest {

    private final UiRegistry registry = new UiRegistry();
    private final CapabilityMcpTools tools = new CapabilityMcpTools(
            new CatalogAccess(new DemoPrincipalUiResolver(registry), new AliasRegistry()));

    @Autowired
    private InvoiceService invoiceService;

    private Registration scope;

    @BeforeEach
    void openTheGridInItsScope() {
        invoiceService.resetAndSeed();
        var view = navigate(AiGridView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());
        scope = view.scope().applyTo(UI.getCurrent());
    }

    @AfterEach
    void liftScope() {
        scope.remove();
    }

    @Test
    void theAiSeesTheFiltersButNotTheGrid() {
        String snapshot = tools.inspect(null, null, null);

        assertTrue(snapshot.contains("grid-filter-statuses"), snapshot);
        assertTrue(snapshot.contains("grid-reset-button"), snapshot);
        assertFalse(snapshot.contains("invoice-grid"), snapshot);
        assertEquals(50, shownInvoices().size());
    }

    /** "Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert" */
    @Test
    void openAcmeInvoicesOverAThousandByDueDate() {
        String snapshot = tools.inspect(null, null, null);

        var results = tools.applyMany(List.of(
                set(snapshot, "grid-filter-supplier", "Acme"),
                set(snapshot, "grid-filter-statuses", "Offen"),
                set(snapshot, "grid-filter-min-gross", "1000"),
                set(snapshot, "grid-filter-sort", "Fälligkeit, früheste zuerst")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        var invoices = shownInvoices();
        // The filters were set inside the closed block, and it stays closed
        var details = find(FilterDetails.class).id("grid-filter-details");
        assertFalse(details.isOpened());
        assertEquals(3, find(Badge.class).withText("aktive Filter").single().getNumber());
        assertFalse(invoices.isEmpty());
        assertTrue(invoices.stream().allMatch(invoice -> invoice.getSupplier().equals("Acme GmbH")
                && invoice.getStatus() == InvoiceStatus.OPEN
                && invoice.getGross().compareTo(new BigDecimal("1000")) >= 0), invoices::toString);
        assertEquals(invoices.stream().sorted(Comparator.comparing(Invoice::getDueDate)).toList(), invoices);
    }

    /** "Ausgaben für Reise und Büro im dritten Quartal", then "nur die unbezahlten" */
    @Test
    void categoriesAndAQuarterThenARefinement() {
        String snapshot = tools.inspect(null, null, null);
        var results = tools.applyMany(List.of(
                set(snapshot, "grid-filter-categories", "Reise"),
                new ApplyStep(selectorFor(snapshot, "grid-filter-categories"), "selectable", "selectByContent",
                        List.of("Büro"), null),
                set(snapshot, "grid-filter-from", "2026-07-01"),
                set(snapshot, "grid-filter-to", "2026-09-30")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);
        int quarter = shownInvoices().size();
        assertEquals(Set.of(InvoiceCategory.TRAVEL, InvoiceCategory.OFFICE),
                shownInvoices().stream().map(Invoice::getCategory).collect(Collectors.toSet()));

        var refined = tools.applyMany(List.of(
                set(snapshot, "grid-filter-statuses", "Offen"),
                new ApplyStep(selectorFor(snapshot, "grid-filter-statuses"), "selectable", "selectByContent",
                        List.of("Überfällig"), null)));
        assertTrue(refined.stream().allMatch(result -> "ok".equals(result.status())), refined::toString);

        var invoices = shownInvoices();
        assertTrue(invoices.size() <= quarter);
        assertTrue(invoices.stream().allMatch(invoice ->
                List.of(InvoiceCategory.TRAVEL, InvoiceCategory.OFFICE).contains(invoice.getCategory())
                        && invoice.getStatus() != InvoiceStatus.PAID
                        && !invoice.getInvoiceDate().isBefore(LocalDate.of(2026, 7, 1))
                        && !invoice.getInvoiceDate().isAfter(LocalDate.of(2026, 9, 30))), invoices::toString);
    }

    @Test
    void resetShowsEverythingAgain() {
        String snapshot = tools.inspect(null, null, null);
        tools.apply(selectorFor(snapshot, "grid-filter-supplier"), "settable", "set", List.of("Acme"), null);

        var reset = tools.apply(selectorFor(snapshot, "grid-reset-button"), "activatable", "activate", null, null);

        assertEquals("ok", reset.status(), reset::toString);
        assertEquals(50, shownInvoices().size());
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

    @SuppressWarnings("unchecked")
    private List<Invoice> shownInvoices() {
        Grid<Invoice> grid = find(Grid.class).id("invoice-grid");
        return grid.getListDataView().getItems().toList();
    }
}
