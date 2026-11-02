package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceCategory;
import com.example.application.ai.invoice.InvoiceFilter;
import com.example.application.ai.invoice.InvoiceService;
import com.example.application.ai.invoice.InvoiceSort;
import com.example.application.ai.invoice.InvoiceStatus;
import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Tab 1: find invoices by asking. The AI does not query anything itself: it sets the ordinary filter bar,
 * and the grid follows every change. It may only touch the filter bar ({@link AgentScope}).
 */
@PageTitle("AI Grid")
@Route("ai/grid")
@Menu(title = "AI Driven UI", icon = LineAwesomeIconUrl.MAGIC_SOLID, order = 0.5)
public class AiGridView extends AiTabView {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    /**
     * How the AI should operate THIS view. Filled in with the previous request, the suppliers, the sort
     * options and the request. draiv's own prompt already tells the model today's date.
     */
    static final String GOAL = """
            The user asks for invoices in plain language. Answer by setting the invoice filter bar on \
            screen; the grid below follows every change by itself, there is no search button.

            The previous request was: "%s". If the new request only adds or changes a criterion \
            ("nur die offenen", "und von Pixelwerk?", "sortiert nach Betrag"), it refines that search: \
            keep every field that is already set and change only what the request mentions. Only when it \
            asks for different invoices altogether, click Zurücksetzen (id grid-reset-button) first.

            Map the request to the fields:
            - grid-filter-supplier (Lieferant): part of a supplier's name, e.g. "Acme". Suppliers: %s.
            - grid-filter-categories (Kategorie): a list from Hardware, Software, Reise, Marketing, Büro.
            - grid-filter-statuses (Status): a list from Offen, Bezahlt, Überfällig. "Unbezahlt" or \
            "noch zu zahlen" means Offen and Überfällig.
            - grid-filter-from, grid-filter-to (Rechnungsdatum von / bis): ISO dates. "Q3" means \
            2026-07-01 to 2026-09-30, "im Oktober" the whole month, "letzte 30 Tage" up to today.
            - grid-filter-min-gross (Betrag ab): the gross amount in euros, a plain number; \
            "über 1.000 €" means 1000.
            - grid-filter-sort (Sortierung): one of %s.

            Be quick, every tool round costs time: inspect once, then make all changes in a single \
            apply_many and do not inspect again. Set fields with capability "settable", operation "set"; \
            click with "activatable", "activate". Kategorie and Status are multi-selects, and "set" takes \
            one label and replaces the whole selection: for several labels, "set" the first, then add each \
            further one with capability "selectable", operation "selectByContent", in the same \
            apply_many; always send the complete selection this way. Do not invent \
            constraints the user did not ask for. Reply in one short German sentence saying what you \
            filtered. Request: "%s"
            """;

    private final InvoiceService invoiceService;
    private final AgentScope scope;
    private final Grid<Invoice> grid = new Grid<>();
    private final Span summary = new Span();

    private final TextField supplier = new TextField("Lieferant");
    private final MultiSelectComboBox<InvoiceCategory> categories = new MultiSelectComboBox<>("Kategorie");
    private final MultiSelectComboBox<InvoiceStatus> statuses = new MultiSelectComboBox<>("Status");
    private final DatePicker from = new DatePicker("Rechnungsdatum von");
    private final DatePicker to = new DatePicker("Rechnungsdatum bis");
    private final BigDecimalField minGross = new BigDecimalField("Betrag ab (€, brutto)");
    private final Select<InvoiceSort> sort = new Select<>();

    private Registration invoiceListener;
    // Each AI run starts without memory, so pass on the previous request to allow "nur die offenen"
    private String previousRequest = "";

    public AiGridView(UiAssistant assistant, InvoiceService invoiceService) {
        super("AI Grid",
                "Rechnungen finden, ohne die Filter selbst zu bedienen: Die AI setzt die Filter über dem Grid.");
        this.invoiceService = invoiceService;

        var filters = createFilterBar();
        // The AI sets the filters; it never touches the grid
        this.scope = AgentScope.within(filters);

        var prompt = AiPromptBar.singleLine(assistant, scope, this::aiGoal, "Suchen");
        prompt.getPrompt().setPlaceholder("z. B. „Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert“");
        prompt.addFallback(FallbackMacros.GRID);

        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Nr.").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Invoice::getSupplier).setHeader("Lieferant").setFlexGrow(2);
        grid.addColumn(invoice -> invoice.getInvoiceDate().format(DATE)).setHeader("Datum").setAutoWidth(true);
        grid.addColumn(invoice -> invoice.getDueDate().format(DATE)).setHeader("Fällig").setAutoWidth(true);
        grid.addColumn(invoice -> EURO.format(invoice.getGross())).setHeader("Brutto")
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        grid.addColumn(invoice -> invoice.getCategory().label()).setHeader("Kategorie").setAutoWidth(true);
        grid.addColumn(invoice -> invoice.getStatus().label()).setHeader("Status").setAutoWidth(true);
        grid.setId("invoice-grid");
        grid.setMinHeight("320px");

        add(prompt, filters, summary, grid);
        setFlexGrow(1, grid);
        setSizeFull();
        refresh();
    }

    private FormLayout createFilterBar() {
        categories.setItems(InvoiceCategory.values());
        categories.setItemLabelGenerator(InvoiceCategory::label);
        statuses.setItems(InvoiceStatus.values());
        statuses.setItemLabelGenerator(InvoiceStatus::label);
        from.setLocale(Locale.GERMANY);
        to.setLocale(Locale.GERMANY);
        sort.setLabel("Sortierung");
        sort.setItems(InvoiceSort.values());
        sort.setItemLabelGenerator(InvoiceSort::label);
        sort.setValue(InvoiceSort.NEWEST);
        sort.setEmptySelectionAllowed(false);
        var reset = new Button("Zurücksetzen", e -> reset());

        // Stable ids, so the AI (and the tests) can target each field precisely
        supplier.setId("grid-filter-supplier");
        categories.setId("grid-filter-categories");
        statuses.setId("grid-filter-statuses");
        from.setId("grid-filter-from");
        to.setId("grid-filter-to");
        minGross.setId("grid-filter-min-gross");
        sort.setId("grid-filter-sort");
        reset.setId("grid-reset-button");

        // No search button: the grid follows every change, the AI's as well as a person's
        for (HasValue<?, ?> field : new HasValue<?, ?>[]{supplier, categories, statuses, from, to, minGross, sort}) {
            field.addValueChangeListener(e -> refresh());
        }
        supplier.setValueChangeMode(ValueChangeMode.LAZY);

        var filters = new FormLayout(supplier, categories, statuses, from, to, minGross, sort, reset);
        filters.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 4));
        filters.setId("grid-filters");
        return filters;
    }

    private String aiGoal(String request) {
        var goal = GOAL.formatted(previousRequest.isEmpty() ? "none" : previousRequest,
                String.join(", ", invoiceService.suppliers()),
                Arrays.stream(InvoiceSort.values()).map(option -> "\"" + option.label() + "\"")
                        .collect(Collectors.joining(", ")),
                request);
        previousRequest = request;
        return goal;
    }

    private void reset() {
        supplier.clear();
        categories.clear();
        statuses.clear();
        from.clear();
        to.clear();
        minGross.clear();
        sort.setValue(InvoiceSort.NEWEST);
    }

    private void refresh() {
        var filter = new InvoiceFilter(supplier.getValue(), categories.getValue(), statuses.getValue(),
                from.getValue(), to.getValue(), minGross.getValue());
        var invoices = invoiceService.find(filter, sort.getValue() == null ? InvoiceSort.NEWEST : sort.getValue());
        grid.setItems(invoices);
        var total = invoices.stream().map(Invoice::getGross).reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setText("%d von %d Rechnungen, zusammen %s brutto".formatted(
                invoices.size(), invoiceService.count(), EURO.format(total)));
    }

    /** New invoices, also from other browsers, appear if they match. */
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
}
