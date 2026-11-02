package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceFilter;
import com.example.application.ai.invoice.InvoiceService;
import com.example.application.ai.invoice.InvoiceSort;
import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.draiv.interaction.CurrentActor;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Tab 6: the AI may only do what the app allows. Here it may search and select invoices, but marking them
 * paid and deleting them are for people. Two layers make sure of that:
 * <ol>
 * <li>an {@link AgentScope} that leaves both buttons out, in effect for <em>every</em> agent driving this
 * UI while the view is open: the prompt bar, the chat and an MCP client alike. The agent cannot see the
 * buttons, and no selector reaches them;</li>
 * <li>the buttons' own handlers refuse when draiv drives the UI ({@link CurrentActor}): draiv's script
 * runner does not apply {@code AgentScope}, so a macro the AI saves and runs could otherwise click them.</li>
 * </ol>
 */
@PageTitle("Guardrails")
@Route("ai/guardrails")
public class AiGuardrailsView extends AiTabView {

    private static final Logger log = LoggerFactory.getLogger(AiGuardrailsView.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    /**
     * The goal says what the user may ask for but not how to get around the guardrails: the scope, not
     * the wording, is what holds. Filled in with the request.
     */
    static final String GOAL = """
            The user asks something about the invoices on screen. Use what you can see: the filters \
            guard-filter-supplier (Lieferant, part of the name) and guard-filter-to (Rechnungsdatum bis, \
            ISO date; "die alten" means older than three months), and selecting rows in the grid. Inspect \
            once and make your changes in one apply_many. If the request asks for something you cannot \
            find a control for, do not look for another way: say in one short German sentence what you did \
            and what a person has to do. Request: "%s"
            """;

    private final InvoiceService invoiceService;
    private final AgentScope scope;
    private final Grid<Invoice> grid = new Grid<>();
    private final TextField supplier = new TextField("Lieferant");
    private final DatePicker to = new DatePicker("Rechnungsdatum bis");
    private final Button markPaid = new Button("Als bezahlt markieren", VaadinIcon.CHECK.create());
    private final Button delete = new Button("Löschen", VaadinIcon.TRASH.create());

    private Registration scopeRegistration;
    private Registration invoiceListener;

    public AiGuardrailsView(UiAssistant assistant, InvoiceService invoiceService) {
        super("Guardrails",
                "Die AI darf nur, was die App erlaubt: Suchen und Auswählen ja, Bezahlt-Markieren und Löschen nein.");
        this.invoiceService = invoiceService;
        // Everything on this view except the two buttons that change data
        this.scope = AgentScope.within(this).excluding(markPaid, delete);

        var prompt = AiPromptBar.singleLine(assistant, scope, GOAL::formatted, "Ausführen");
        prompt.getPrompt().setPlaceholder("z. B. „Markiere alle als bezahlt und lösch die alten“");

        supplier.setId("guard-filter-supplier");
        supplier.setValueChangeMode(ValueChangeMode.LAZY);
        supplier.addValueChangeListener(e -> refresh());
        to.setId("guard-filter-to");
        to.setLocale(Locale.GERMANY);
        to.addValueChangeListener(e -> refresh());

        markPaid.setId("guard-mark-paid");
        markPaid.addClickListener(e -> forPeopleOnly("als bezahlt markieren", invoiceService::markPaid));
        delete.setId("guard-delete");
        delete.addThemeVariants(ButtonVariant.ERROR);
        delete.addClickListener(e -> forPeopleOnly("löschen", invoiceService::delete));
        for (var button : new Button[]{markPaid, delete}) {
            button.addClassName("ai-off-limits");
            button.setTooltipText("Für die AI gesperrt");
        }

        var toolbar = new HorizontalLayout(supplier, to, markPaid, delete);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        toolbar.setWrap(true);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Nr.").setAutoWidth(true);
        grid.addColumn(Invoice::getSupplier).setHeader("Lieferant").setFlexGrow(2);
        grid.addColumn(invoice -> invoice.getInvoiceDate().format(DATE)).setHeader("Datum").setAutoWidth(true);
        grid.addColumn(invoice -> EURO.format(invoice.getGross())).setHeader("Brutto")
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        grid.addColumn(invoice -> invoice.getStatus().label()).setHeader("Status").setAutoWidth(true);
        grid.setId("guard-grid");
        grid.setMinHeight("320px");

        add(prompt, rules(), toolbar, grid);
        setFlexGrow(1, grid);
        setSizeFull();
        refresh();
    }

    /** What the AI may and may not do here, for the visitors. */
    private static Div rules() {
        var allowed = new UnorderedList(new ListItem("Rechnungen filtern"), new ListItem("Zeilen auswählen"));
        var forbidden = new UnorderedList(new ListItem("als bezahlt markieren"), new ListItem("löschen"));
        var rules = new Div(new Span("Die AI darf:"), allowed, new Span("Die AI darf nicht:"), forbidden);
        rules.addClassName("ai-guardrail-rules");
        // Information for people; the AI should not read its own rules off the screen
        rules.getElement().setAttribute("data-draiv-control", "true");
        return rules;
    }

    /**
     * Runs a data-changing action for the selected invoices, unless draiv is driving the UI: the second
     * guardrail, for draiv's script runner, which does not apply the {@link AgentScope}.
     */
    private void forPeopleOnly(String what, Consumer<Set<Invoice>> action) {
        var ui = UI.getCurrent();
        if (CurrentActor.isProgrammatic(ui)) {
            log.warn("Refused to {} for {}", what, CurrentActor.get(ui));
            Notification.show("Die AI darf Rechnungen nicht " + what + ".")
                    .addThemeVariants(NotificationVariant.ERROR);
            return;
        }
        var selected = grid.getSelectedItems();
        if (selected.isEmpty()) {
            Notification.show("Erst Rechnungen auswählen.");
            return;
        }
        action.accept(selected);
        Notification.show(selected.size() + " Rechnung(en) " + (what.equals("löschen") ? "gelöscht." : "als bezahlt markiert."));
        refresh();
    }

    private void refresh() {
        grid.setItems(invoiceService.find(new InvoiceFilter(supplier.getValue(), Set.of(), Set.of(), null,
                to.getValue(), null), InvoiceSort.NEWEST));
    }

    /** The scope holds for every agent on this UI while the view is open, not only during a prompt run. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        var ui = attachEvent.getUI();
        scopeRegistration = scope.applyTo(ui);
        invoiceListener = invoiceService.addInvoiceListener(invoice -> ui.access(this::refresh));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        scopeRegistration.remove();
        invoiceListener.remove();
        super.onDetach(detachEvent);
    }
}
