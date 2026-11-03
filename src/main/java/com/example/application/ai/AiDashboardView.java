package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceCategory;
import com.example.application.ai.invoice.InvoiceFilter;
import com.example.application.ai.invoice.InvoiceGrouping;
import com.example.application.ai.invoice.InvoiceService;
import com.example.application.ai.invoice.InvoiceStatus;
import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tab 4: analyse spending by saying what you want to see. The AI sets the ordinary control bar (what to
 * add up, chart type, status, categories, period); the chart listens to the bar's value changes. draiv
 * does not drive the chart itself, and the AI may only touch the control bar ({@link AgentScope}), which
 * stays folded away in a {@link FilterDetails}.
 */
@PageTitle("AI Dashboard")
@Route("ai/dashboard")
public class AiDashboardView extends AiTabView {

    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    /** The chart types on offer, by the label the control bar and the AI use. */
    enum Kind {
        BAR("Balken", ChartType.BAR), COLUMN("Säulen", ChartType.COLUMN), PIE("Torte", ChartType.PIE),
        LINE("Linie", ChartType.LINE);

        final String label;
        final ChartType type;

        Kind(String label, ChartType type) {
            this.label = label;
            this.type = type;
        }
    }

    /**
     * How the AI should operate THIS view. Filled in with the previous request, the evaluations, the chart
     * types and the request. draiv's own prompt already tells the model today's date.
     */
    static final String GOAL = """
            The user describes a spending chart in plain language. Answer by setting the dashboard's \
            control bar on screen; the chart follows every change by itself.

            The previous request was: "%s". If the new request only changes something ("als Torte", \
            "jetzt mit den bezahlten", "nur Q4"), keep every other field as it is. Only when it asks for a \
            different chart altogether, click Zurücksetzen (id dashboard-reset-button) first.

            Map the request to the fields:
            - dashboard-grouping (Auswertung): one of %s.
            - dashboard-chart-type (Diagramm): one of %s. "Balken" are horizontal bars, "Säulen" \
            vertical ones; use Linie only for Ausgaben pro Monat.
            - dashboard-statuses (Status): checkboxes Offen, Bezahlt, Überfällig; none checked means all. \
            "Unbezahlt" or "offene Posten" means Offen and Überfällig.
            - dashboard-categories (Kategorie): checkboxes Hardware, Software, Reise, Marketing, Büro; \
            none checked means all.
            - dashboard-from, dashboard-to (Rechnungsdatum von / bis): ISO dates. "Q3" means 2026-07-01 \
            to 2026-09-30, "im Oktober" the whole month, "dieses Jahr" from 2026-01-01.

            Be quick, every tool round costs time: inspect once, then make all changes in a single \
            apply_many and do not inspect again. Set fields with capability "settable", operation "set"; \
            click with "activatable", "activate". Status and Kategorie are checkbox groups, and "set" takes \
            one label and replaces the whole selection: for several labels, "set" the first, then add each \
            further one with capability "selectable", operation "selectByContent", in the same apply_many; \
            always send the complete selection this way. Do not invent constraints the user did not ask \
            for. Reply in one short German sentence saying what the chart shows. Request: "%s"
            """;

    private final InvoiceService invoiceService;
    private final AgentScope scope;
    private final Div chartArea = new Div();
    private final Span summary = new Span();

    private final Select<InvoiceGrouping> grouping = new Select<>();
    private final Select<Kind> kind = new Select<>();
    private final CheckboxGroup<InvoiceStatus> statuses = new CheckboxGroup<>("Status");
    private final CheckboxGroup<InvoiceCategory> categories = new CheckboxGroup<>("Kategorie");
    private final DatePicker from = new DatePicker("Rechnungsdatum von");
    private final DatePicker to = new DatePicker("Rechnungsdatum bis");

    private Registration invoiceListener;
    // Each AI run starts without memory, so pass on the previous request to allow "als Torte"
    private String previousRequest = "";

    public AiDashboardView(UiAssistant assistant, InvoiceService invoiceService) {
        super("AI Dashboard",
                "Ausgaben per Satz auswerten: Die AI stellt die Steuerleiste ein, das Diagramm folgt.");
        this.invoiceService = invoiceService;

        var controls = createControlBar();
        // The AI sets the controls; the chart only listens to them
        this.scope = AgentScope.within(controls);

        var prompt = AiPromptBar.singleLine(assistant, scope, this::aiGoal, "Auswerten");
        prompt.getPrompt().setPlaceholder("z. B. „Ausgaben pro Kategorie als Balken, nur offene aus Q3“");

        chartArea.addClassName("ai-dashboard-chart");
        chartArea.setId("dashboard-chart");
        chartArea.setWidthFull();

        // The controls stay folded away; the key figures stay in sight next to them
        var controlDetails = new FilterDetails(controls, this::activeFilters,
                grouping, kind, statuses, categories, from, to);
        controlDetails.setId("dashboard-filter-details");
        summary.addClassName("filter-result");
        var controlRow = new HorizontalLayout(controlDetails, summary);
        controlRow.setAlignItems(Alignment.BASELINE);
        controlRow.setWidthFull();
        controlRow.setFlexGrow(1, controlDetails);

        add(prompt, controlRow, chartArea);
        refresh();
    }

    private FormLayout createControlBar() {
        grouping.setLabel("Auswertung");
        grouping.setItems(InvoiceGrouping.values());
        grouping.setItemLabelGenerator(InvoiceGrouping::label);
        grouping.setValue(InvoiceGrouping.CATEGORY);
        grouping.setEmptySelectionAllowed(false);
        kind.setLabel("Diagramm");
        kind.setItems(Kind.values());
        kind.setItemLabelGenerator(option -> option.label);
        kind.setValue(Kind.COLUMN);
        kind.setEmptySelectionAllowed(false);
        statuses.setItems(InvoiceStatus.values());
        statuses.setItemLabelGenerator(InvoiceStatus::label);
        categories.setItems(InvoiceCategory.values());
        categories.setItemLabelGenerator(InvoiceCategory::label);
        from.setLocale(Locale.GERMANY);
        to.setLocale(Locale.GERMANY);
        var reset = new Button("Zurücksetzen", e -> reset());

        // Stable ids, so the AI (and the tests) can target each field precisely
        grouping.setId("dashboard-grouping");
        kind.setId("dashboard-chart-type");
        statuses.setId("dashboard-statuses");
        categories.setId("dashboard-categories");
        from.setId("dashboard-from");
        to.setId("dashboard-to");
        reset.setId("dashboard-reset-button");

        for (HasValue<?, ?> field : new HasValue<?, ?>[]{grouping, kind, statuses, categories, from, to}) {
            field.addValueChangeListener(e -> refresh());
        }

        var controls = new FormLayout(grouping, kind, from, to, statuses, categories, reset);
        controls.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 4));
        controls.setColspan(statuses, 2);
        controls.setColspan(categories, 2);
        controls.setId("dashboard-controls");
        return controls;
    }

    private String aiGoal(String request) {
        var goal = GOAL.formatted(previousRequest.isEmpty() ? "none" : previousRequest,
                Arrays.stream(InvoiceGrouping.values()).map(option -> "\"" + option.label() + "\"")
                        .collect(Collectors.joining(", ")),
                Arrays.stream(Kind.values()).map(option -> "\"" + option.label + "\"")
                        .collect(Collectors.joining(", ")),
                request);
        previousRequest = request;
        return goal;
    }

    /** The filters that narrow the invoices; what is added up and the chart type are not filters. */
    private int activeFilters() {
        int active = 0;
        active += statuses.getValue().isEmpty() ? 0 : 1;
        active += categories.getValue().isEmpty() ? 0 : 1;
        active += from.getValue() == null ? 0 : 1;
        active += to.getValue() == null ? 0 : 1;
        return active;
    }

    private void reset() {
        grouping.setValue(InvoiceGrouping.CATEGORY);
        kind.setValue(Kind.COLUMN);
        statuses.clear();
        categories.clear();
        from.clear();
        to.clear();
    }

    /** Rebuilds the chart from the control bar: a fresh Chart per change, so the type can change too. */
    private void refresh() {
        var filter = new InvoiceFilter(null, categories.getValue(), statuses.getValue(),
                from.getValue(), to.getValue(), null);
        var totals = invoiceService.totals(filter, grouping.getValue());
        var chart = createChart(totals);
        chartArea.removeAll();
        chartArea.add(chart);
        var sum = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setText(totals.isEmpty() ? "Keine Rechnungen für diese Auswahl."
                : "%s: zusammen %s brutto".formatted(grouping.getValue().label(), EURO.format(sum)));
    }

    private Chart createChart(Map<String, BigDecimal> totals) {
        var chart = new Chart(kind.getValue().type);
        var configuration = chart.getConfiguration();
        configuration.setTitle(grouping.getValue().label());
        configuration.getxAxis().setType(AxisType.CATEGORY);
        configuration.getyAxis().setTitle("€ brutto");
        configuration.getLegend().setEnabled(kind.getValue() == Kind.PIE);
        var series = new DataSeries("Brutto");
        totals.forEach((group, total) -> series.add(new DataSeriesItem(group, total)));
        configuration.addSeries(series);
        chart.setWidthFull();
        chart.setHeight("420px");
        return chart;
    }

    /** New invoices, also from other browsers, count right away. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        var ui = attachEvent.getUI();
        invoiceListener = invoiceService.addInvoiceListener(invoice -> ui.access(this::refresh));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        invoiceListener.remove();
        super.onDetach(detachEvent);
    }

    /** What the AI may touch; for the tests. */
    AgentScope scope() {
        return scope;
    }

    /** The data the chart shows, group by group; for the tests. */
    Map<String, Number> shownTotals() {
        var chart = (Chart) chartArea.getChildren().findFirst().orElseThrow();
        var series = (DataSeries) chart.getConfiguration().getSeries().getFirst();
        return series.getData().stream().collect(Collectors.toMap(DataSeriesItem::getName, DataSeriesItem::getY,
                (a, b) -> a, LinkedHashMap::new));
    }

    /** The chart type on screen; for the tests. */
    ChartType shownType() {
        var chart = (Chart) chartArea.getChildren().findFirst().orElseThrow();
        return chart.getConfiguration().getChart().getType();
    }
}
