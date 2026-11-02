package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceChange;
import com.example.application.ai.invoice.InvoiceForm;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.draiv.interaction.CurrentActor;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

/**
 * Tab 3: all invoices, master-detail. Search, pick a row to edit it, "Neu" for a new one. Built from plain
 * components rather than Vaadin's Crud, so draiv drives it with the capabilities it knows: a macro can
 * search, select a row, change fields and save. Deleting and the status are for people only: both are
 * draiv control chrome, out of every agent's sight, and the delete handler also refuses when draiv drives
 * the UI ({@link CurrentActor}), since draiv's script runner ignores scopes.
 */
@PageTitle("Rechnungen")
@Route("ai/invoices")
public class AiInvoiceCrudView extends AiTabView {

    private static final Logger log = LoggerFactory.getLogger(AiInvoiceCrudView.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private final InvoiceService invoiceService;
    private final TextField search = new TextField();
    private final Grid<Invoice> grid = new Grid<>();
    private final InvoiceForm form;
    private final H3 editorTitle = new H3();
    private final Button delete = new Button("Löschen", VaadinIcon.TRASH.create());

    private Registration invoiceListener;
    /** The invoice created or changed last while this view is open; its row is highlighted. */
    private UUID changed;

    public AiInvoiceCrudView(InvoiceService invoiceService) {
        super("Rechnungen", "Alle Rechnungen: suchen, bearbeiten, neu anlegen. Löschen dürfen nur Menschen.");
        this.invoiceService = invoiceService;
        this.form = InvoiceForm.editor(invoiceService);

        search.setPlaceholder("Rechnungsnummer oder Lieferant");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());
        search.setId("invoice-search");
        search.setWidthFull();

        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Nr.").setComparator(Invoice::getInvoiceNumber)
                .setAutoWidth(true);
        grid.addColumn(Invoice::getSupplier).setHeader("Lieferant").setComparator(Invoice::getSupplier)
                .setFlexGrow(2);
        grid.addColumn(invoice -> invoice.getInvoiceDate().format(DATE)).setHeader("Datum")
                .setComparator(Invoice::getInvoiceDate).setAutoWidth(true);
        grid.addColumn(invoice -> EURO.format(invoice.getGross())).setHeader("Brutto")
                .setComparator(Comparator.comparing(Invoice::getGross)).setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true);
        grid.addColumn(invoice -> invoice.getCategory().label()).setHeader("Kategorie")
                .setComparator(Invoice::getCategory).setAutoWidth(true);
        grid.addColumn(invoice -> invoice.getStatus().label()).setHeader("Status")
                .setComparator(Invoice::getStatus).setAutoWidth(true);
        grid.setPartNameGenerator(invoice -> invoice.getId().equals(changed) ? "new-invoice" : null);
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() == null) {
                createNew();
            } else {
                edit(e.getValue());
            }
        });
        grid.setId("invoice-crud-grid");
        grid.setMinHeight("360px");

        // The status is for people: no agent sees or sets it here
        form.getStatusField().getElement().setAttribute("data-draiv-control", "true");

        var newButton = new Button("Neu", VaadinIcon.PLUS.create(), e -> grid.asSingleSelect().clear());
        newButton.setId("invoice-new-button");
        var save = new Button("Speichern", VaadinIcon.CHECK.create(), e -> save());
        save.setId("invoice-save-button");
        save.addThemeVariants(ButtonVariant.PRIMARY);
        delete.setId("invoice-delete-button");
        delete.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.TERTIARY);
        delete.addClickListener(e -> confirmDelete());
        delete.addClassName("ai-off-limits");
        delete.setTooltipText("Nur für Menschen");
        delete.getElement().setAttribute("data-draiv-control", "true");
        var buttons = new HorizontalLayout(save, newButton, delete);

        var master = new Div(search, grid);
        master.addClassName("ai-crud-master");
        var editor = new Div(editorTitle, form, buttons);
        editor.addClassName("ai-crud-editor");
        editor.setId("invoice-editor");
        var columns = new Div(master, editor);
        columns.addClassName("ai-crud");
        add(columns);

        createNew();
        refresh();
    }

    private void createNew() {
        form.create();
        editorTitle.setText("Neue Rechnung");
        delete.setEnabled(false);
    }

    private void edit(Invoice invoice) {
        form.edit(invoice);
        editorTitle.setText("Rechnung " + invoice.getInvoiceNumber());
        delete.setEnabled(true);
    }

    private void save() {
        boolean isNew = form.getEdited().getId() == null;
        form.save().ifPresent(saved -> {
            Notification.show("Rechnung " + saved.getInvoiceNumber() + (isNew ? " angelegt." : " gespeichert."))
                    .addThemeVariants(NotificationVariant.SUCCESS);
            refresh();
            grid.select(saved);
        });
    }

    private void confirmDelete() {
        var invoice = form.getEdited();
        if (invoice.getId() == null) {
            return;
        }
        if (refusedForAgents()) {
            return;
        }
        var confirm = new ConfirmDialog("Rechnung löschen?",
                "Rechnung " + invoice.getInvoiceNumber() + " von " + invoice.getSupplier() + " wird gelöscht.",
                "Löschen", confirmed -> {
                    if (refusedForAgents()) {
                        return;
                    }
                    invoiceService.delete(invoice);
                    Notification.show("Rechnung " + invoice.getInvoiceNumber() + " gelöscht.");
                    grid.asSingleSelect().clear();
                    refresh();
                });
        confirm.setConfirmButtonTheme("error primary");
        confirm.setCancelable(true);
        confirm.setCancelText("Abbrechen");
        confirm.open();
    }

    /** The second guardrail: draiv's script runner does not respect data-draiv-control on the button. */
    private static boolean refusedForAgents() {
        var ui = UI.getCurrent();
        if (CurrentActor.isProgrammatic(ui)) {
            log.warn("Refused to delete an invoice for {}", CurrentActor.get(ui));
            Notification.show("Die AI darf keine Rechnungen löschen.").addThemeVariants(NotificationVariant.ERROR);
            return true;
        }
        return false;
    }

    private void refresh() {
        grid.setItems(invoiceService.search(search.getValue()));
    }

    /** Changes from any browser show up right away; the changed row is highlighted. */
    private void changed(InvoiceChange change) {
        if (change.kind() != InvoiceChange.Kind.DELETED) {
            changed = change.invoice().getId();
        }
        var selected = grid.asSingleSelect().getValue();
        refresh();
        if (selected != null && change.kind() == InvoiceChange.Kind.DELETED
                && selected.getId().equals(change.invoice().getId())) {
            grid.asSingleSelect().clear();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        var ui = attachEvent.getUI();
        invoiceListener = invoiceService.addInvoiceListener(change -> ui.access(() -> changed(change)));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        invoiceListener.remove();
        super.onDetach(detachEvent);
    }
}
