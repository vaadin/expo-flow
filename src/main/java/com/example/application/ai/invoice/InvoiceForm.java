package com.example.application.ai.invoice;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Enter a new invoice. People and the AI use the very same fields: the AI fills them in through draiv
 * and the same validation applies. The buttons are fields with getters so a view can leave them out
 * of what the AI may touch ({@code AgentScope.within(form).excluding(form.getAddButton())}).
 */
public class InvoiceForm extends FormLayout {

    private static final Locale GERMAN = Locale.GERMANY;

    private final TextField invoiceNumber = new TextField("Rechnungsnummer");
    // A text field, not a ComboBox: draiv only sets a ComboBox to one of its items, and an
    // uploaded invoice often comes from a supplier that is not in the list yet
    private final TextField supplier = new TextField("Lieferant");
    private final DatePicker invoiceDate = new DatePicker("Rechnungsdatum");
    private final DatePicker dueDate = new DatePicker("Fällig am");
    private final BigDecimalField net = new BigDecimalField("Netto (€)");
    private final Select<VatRate> vatRate = new Select<>();
    // Not bound: shown only, computed from net and VAT rate like Invoice.getGross()
    private final TextField grossDisplay = new TextField("Brutto (€)");
    private final Select<InvoiceCategory> category = new Select<>();
    private final RadioButtonGroup<InvoiceStatus> status = new RadioButtonGroup<>("Status");
    private final TextField receipt = new TextField("Beleg");
    private final Button addButton = new Button("Hinzufügen");
    private final Button resetButton = new Button("Zurücksetzen");

    private final InvoiceService invoiceService;
    private final BeanValidationBinder<Invoice> binder = new BeanValidationBinder<>(Invoice.class);
    private final List<Consumer<Invoice>> addedListeners = new ArrayList<>();

    public InvoiceForm(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;

        invoiceNumber.setPlaceholder("z. B. RE-2026-114");
        invoiceDate.setLocale(GERMAN);
        dueDate.setLocale(GERMAN);
        vatRate.setLabel("MwSt-Satz");
        vatRate.setItems(VatRate.values());
        vatRate.setItemLabelGenerator(VatRate::label);
        grossDisplay.setReadOnly(true);
        category.setLabel("Kategorie");
        category.setItems(InvoiceCategory.values());
        category.setItemLabelGenerator(InvoiceCategory::label);
        status.setItems(InvoiceStatus.values());
        status.setItemLabelGenerator(InvoiceStatus::label);
        receipt.setPlaceholder("Dateiname, optional");

        // Stable ids, so the AI (and the tests) can target each field precisely
        invoiceNumber.setId("invoice-number");
        supplier.setId("invoice-supplier");
        invoiceDate.setId("invoice-date");
        dueDate.setId("invoice-due-date");
        net.setId("invoice-net");
        vatRate.setId("invoice-vat-rate");
        grossDisplay.setId("invoice-gross");
        category.setId("invoice-category");
        status.setId("invoice-status");
        receipt.setId("invoice-receipt");
        addButton.setId("invoice-add-button");
        resetButton.setId("invoice-reset-button");

        binder.bindInstanceFields(this);
        net.addValueChangeListener(e -> updateGross());
        vatRate.addValueChangeListener(e -> updateGross());

        addButton.addThemeVariants(ButtonVariant.PRIMARY);
        addButton.addClickListener(e -> addInvoice());
        resetButton.addClickListener(e -> clear());
        var buttons = new HorizontalLayout(addButton, resetButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2));
        add(invoiceNumber, supplier, invoiceDate, dueDate, net, vatRate, grossDisplay, category, receipt);
        add(status, 2);
        add(buttons, 2);

        clear();
    }

    private void updateGross() {
        var value = net.getValue();
        var rate = vatRate.getValue();
        grossDisplay.setValue(value == null || rate == null ? ""
                : NumberFormat.getCurrencyInstance(GERMAN).format(
                        value.add(value.multiply(rate.factor()))));
    }

    private void addInvoice() {
        var invoice = new Invoice();
        if (!binder.writeBeanIfValid(invoice)) {
            Notification.show("Bitte die markierten Felder ausfüllen.")
                    .addThemeVariants(NotificationVariant.WARNING);
            return;
        }
        var saved = invoiceService.save(invoice);
        Notification.show("Rechnung " + invoice.getInvoiceNumber() + " hinzugefügt.")
                .addThemeVariants(NotificationVariant.SUCCESS);
        clear();
        addedListeners.forEach(listener -> listener.accept(saved));
    }

    /** Called after "Hinzufügen" saved a new invoice, e.g. to clear a receipt preview. */
    public Registration addInvoiceAddedListener(Consumer<Invoice> listener) {
        addedListeners.add(listener);
        return () -> addedListeners.remove(listener);
    }

    /** Back to an empty invoice: 19 % VAT and status open. */
    public void clear() {
        binder.readBean(new Invoice());
        updateGross();
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

    public RadioButtonGroup<InvoiceStatus> getStatusField() {
        return status;
    }
}
