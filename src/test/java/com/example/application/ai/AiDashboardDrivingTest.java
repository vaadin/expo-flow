package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
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
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.shared.Registration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the dashboard's control bar as its AI run does: with the view's scope in effect, {@code inspect}
 * once, then one {@code apply_many}. No model in the loop; the chart has to follow the bar.
 */
@SpringBootTest
class AiDashboardDrivingTest extends SpringBrowserlessTest {

    private final UiRegistry registry = new UiRegistry();
    private final CapabilityMcpTools tools = new CapabilityMcpTools(
            new CatalogAccess(new DemoPrincipalUiResolver(registry), new AliasRegistry()));

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceRepository invoices;

    private AiDashboardView view;
    private Registration scope;

    @BeforeEach
    void openTheDashboardInItsScope() {
        invoiceService.resetAndSeed();
        view = navigate(AiDashboardView.class);
        registry.register(DemoPrincipal.ID, UI.getCurrent());
        scope = view.scope().applyTo(UI.getCurrent());
    }

    @AfterEach
    void liftScope() {
        scope.remove();
    }

    @Test
    void theAiSeesTheControlsButNotTheChart() {
        String snapshot = tools.inspect(null, null, null);

        assertTrue(snapshot.contains("dashboard-statuses"), snapshot);
        assertFalse(snapshot.contains("dashboard-chart\""), snapshot);
        assertEquals(ChartType.COLUMN, view.shownType());
        assertEquals(5, view.shownTotals().size()); // all five categories
    }

    /** "Ausgaben pro Kategorie als Balken, nur offene aus Q3" */
    @Test
    void openSpendingPerCategoryInTheThirdQuarterAsBars() {
        String snapshot = tools.inspect(null, null, null);

        var results = tools.applyMany(List.of(
                set(snapshot, "dashboard-grouping", "Ausgaben pro Kategorie"),
                set(snapshot, "dashboard-chart-type", "Balken"),
                set(snapshot, "dashboard-statuses", "Offen"),
                set(snapshot, "dashboard-from", "2026-07-01"),
                set(snapshot, "dashboard-to", "2026-09-30")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        assertEquals(ChartType.BAR, view.shownType());
        assertEquals(expectedPerCategory(invoice -> invoice.getStatus() == InvoiceStatus.OPEN
                && !invoice.getInvoiceDate().isBefore(LocalDate.of(2026, 7, 1))
                && !invoice.getInvoiceDate().isAfter(LocalDate.of(2026, 9, 30))), shownAsDecimals());
    }

    /** "als Torte, mit den überfälligen": a refinement keeps the rest and adds a second status */
    @Test
    void aRefinementChangesTypeAndAddsAStatus() {
        String snapshot = tools.inspect(null, null, null);
        tools.apply(selectorFor(snapshot, "dashboard-statuses"), "settable", "set", List.of("Offen"), null);

        var results = tools.applyMany(List.of(
                set(snapshot, "dashboard-chart-type", "Torte"),
                set(snapshot, "dashboard-statuses", "Offen"),
                new ApplyStep(selectorFor(snapshot, "dashboard-statuses"), "selectable", "selectByContent",
                        List.of("Überfällig"), null)));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        assertEquals(ChartType.PIE, view.shownType());
        assertEquals(expectedPerCategory(invoice -> invoice.getStatus() != InvoiceStatus.PAID), shownAsDecimals());
    }

    @Test
    void spendingPerMonthAsALine() {
        String snapshot = tools.inspect(null, null, null);

        var results = tools.applyMany(List.of(
                set(snapshot, "dashboard-grouping", "Ausgaben pro Monat"),
                set(snapshot, "dashboard-chart-type", "Linie")));
        assertTrue(results.stream().allMatch(result -> "ok".equals(result.status())), results::toString);

        assertEquals(ChartType.LINE, view.shownType());
        assertTrue(view.shownTotals().size() >= 6, view.shownTotals()::toString); // half a year of invoices
    }

    private Map<String, BigDecimal> expectedPerCategory(java.util.function.Predicate<Invoice> matches) {
        return invoices.findAll().stream().filter(matches)
                .collect(Collectors.groupingBy(invoice -> invoice.getCategory().label(),
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getGross, BigDecimal::add)));
    }

    private Map<String, BigDecimal> shownAsDecimals() {
        return view.shownTotals().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> new BigDecimal(entry.getValue().toString())));
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
