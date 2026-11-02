package com.example.application.ai;

import com.example.application.ai.invoice.Invoice;
import com.example.application.ai.invoice.InvoiceForm;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Tab 3: an invoice goes into the chat as a PDF or photo, and the AI enters it with this form. For
 * now the form and the latest invoices; reading the upload follows in a later step.
 */
@PageTitle("Rechnung hochladen")
@Route("ai/upload")
public class AiInvoiceUploadView extends AiTabView {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private final InvoiceService invoiceService;
    private final Grid<Invoice> latest = new Grid<>();
    private Registration invoiceListener;
    /** The invoice added last while this view is open; its row is highlighted. */
    private UUID newest;

    public AiInvoiceUploadView(InvoiceService invoiceService) {
        super("Rechnung hochladen",
                "PDF oder Foto einer Rechnung in den Chat ziehen: Die AI trägt sie hier ein und fragt vor dem Hinzufügen nach.");
        this.invoiceService = invoiceService;

        latest.addColumn(Invoice::getInvoiceNumber).setHeader("Nr.").setAutoWidth(true);
        latest.addColumn(Invoice::getSupplier).setHeader("Lieferant").setFlexGrow(2);
        latest.addColumn(invoice -> invoice.getInvoiceDate().format(DATE)).setHeader("Datum").setAutoWidth(true);
        latest.addColumn(invoice -> EURO.format(invoice.getGross())).setHeader("Brutto")
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true);
        latest.addColumn(invoice -> invoice.getCategory().label()).setHeader("Kategorie").setAutoWidth(true);
        latest.addColumn(invoice -> invoice.getStatus().label()).setHeader("Status").setAutoWidth(true);
        latest.setPartNameGenerator(invoice -> invoice.getId().equals(newest) ? "new-invoice" : null);
        latest.setAllRowsVisible(true);
        latest.setId("latest-invoices");
        showLatest();

        add(new InvoiceForm(invoiceService), new H3("Neueste Rechnungen"), latest);
    }

    private void showLatest() {
        latest.setItems(invoiceService.latest());
    }

    /**
     * New invoices, from this tab or any other browser (the phone at the booth), show up here right away
     * and are highlighted.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        var ui = attachEvent.getUI();
        invoiceListener = invoiceService.addInvoiceListener(invoice -> ui.access(() -> {
            newest = invoice.getId();
            showLatest();
        }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        invoiceListener.remove();
        super.onDetach(detachEvent);
    }
}
