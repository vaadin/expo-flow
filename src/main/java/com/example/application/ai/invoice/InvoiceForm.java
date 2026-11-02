package com.example.application.ai.invoice;

import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.shared.Registration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Enter a new invoice or change one. People and the AI use the very same fields: the AI fills them in
 * through draiv and the same validation applies. The buttons are fields with getters so a view can leave
 * them out of what the AI may touch ({@code AgentScope.within(form).excluding(form.getAddButton())}).
 * <p>
 * Two flavours, with the same field ids: {@code new InvoiceForm(...)} adds new invoices with its own
 * "Hinzufügen" and "Zurücksetzen" (tab 2); {@link #editor(InvoiceService)} has no buttons, and the
 * view around it calls {@link #edit(Invoice)}, {@link #create()} and {@link #save()} (tab 3).
 */
public class InvoiceForm extends FormLayout {

    private static final Locale GERMAN = Locale.GERMANY;

    private final TextField invoiceNumber = new TextField("Rechnungsnummer");
    // A text field, not a ComboBox: draiv only sets a ComboBox to one of its items, and an
    // uploaded invoice often comes from a supplier that is not in the list yet
    private final TextField supplier = new TextField("Lieferant");
    private final EmailField contactEmail = new EmailField("Kontakt-E-Mail");
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
    /** The invoice the fields show: a new, unsaved one or the one being edited. */
    private Invoice edited = new Invoice();

    /** A form that adds new invoices, with "Hinzufügen" and "Zurücksetzen". */
    public InvoiceForm(InvoiceService invoiceService) {
        this(invoiceService, true);
    }

    /** A form without buttons, for a view that edits existing invoices too. */
    public static InvoiceForm editor(InvoiceService invoiceService) {
        return new InvoiceForm(invoiceService, false);
    }

    private InvoiceForm(InvoiceService invoiceService, boolean withButtons) {
        this.invoiceService = invoiceService;

        invoiceNumber.setPlaceholder("z. B. RE-2026-114");
        contactEmail.setPlaceholder("optional, z. B. rechnung@lieferant.de");
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
        contactEmail.setId("invoice-contact-email");
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
        add(invoiceNumber, supplier, contactEmail, invoiceDate, dueDate, net, vatRate, grossDisplay, category, receipt);
        add(status, 2);
        if (withButtons) {
            add(buttons, 2);
        }

        create();
    }

    private void updateGross() {
        var value = net.getValue();
        var rate = vatRate.getValue();
        grossDisplay.setValue(value == null || rate == null ? ""
                : NumberFormat.getCurrencyInstance(GERMAN).format(
                        value.add(value.multiply(rate.factor()))));
    }

    private void addInvoice() {
        edited = new Invoice(); // "Hinzufügen" always adds a new invoice from what the fields show
        save().ifPresent(saved -> {
            Notification.show("Rechnung " + saved.getInvoiceNumber() + " hinzugefügt.")
                    .addThemeVariants(NotificationVariant.SUCCESS);
            create();
            addedListeners.forEach(listener -> listener.accept(saved));
        });
    }

    /**
     * Writes the fields into the invoice shown and saves it: a new one is added, an edited one updated.
     * Empty when a field is invalid; the fields show why.
     */
    public Optional<Invoice> save() {
        if (!binder.writeBeanIfValid(edited)) {
            Notification.show("Bitte die markierten Felder ausfüllen.")
                    .addThemeVariants(NotificationVariant.WARNING);
            return Optional.empty();
        }
        edited = invoiceService.save(edited);
        return Optional.of(edited);
    }

    /** Called after "Hinzufügen" saved a new invoice, e.g. to clear a receipt preview. */
    public Registration addInvoiceAddedListener(Consumer<Invoice> listener) {
        addedListeners.add(listener);
        return () -> addedListeners.remove(listener);
    }

    /** An empty new invoice: 19 % VAT and status open. */
    public void create() {
        edited = new Invoice();
        binder.readBean(edited);
        updateGross();
        clearEvidence();
    }

    /** Shows {@code invoice} for changing it; {@link #save()} then updates it. */
    public void edit(Invoice invoice) {
        edited = invoice;
        binder.readBean(edited);
        updateGross();
        clearEvidence();
    }

    /**
     * Under each field the draft has a value for: how sure the extraction is (a badge) and the text on the
     * receipt the value comes from. It stays until the form is cleared, also when a person corrects the
     * value: it describes what was read, not what is entered now.
     */
    public void showEvidence(InvoiceDraft draft) {
        evidenceFields().forEach((field, component) -> component.setHelperComponent(
                draft.evidenceFor(field).map(InvoiceForm::evidenceView).orElse(null)));
    }

    private void clearEvidence() {
        evidenceFields().values().forEach(component -> component.setHelperComponent(null));
    }

    /** The form label of a draft field, e.g. "Fällig am" for dueDate. */
    public String labelOf(String field) {
        var component = evidenceFields().get(field);
        return component instanceof HasLabel labelled ? labelled.getLabel() : field;
    }

    /** The fields that show evidence, by {@link InvoiceDraft} field name. */
    private Map<String, HasHelper> evidenceFields() {
        var fields = new LinkedHashMap<String, HasHelper>();
        fields.put("invoiceNumber", invoiceNumber);
        fields.put("supplier", supplier);
        fields.put("contactEmail", contactEmail);
        fields.put("invoiceDate", invoiceDate);
        fields.put("dueDate", dueDate);
        fields.put("net", net);
        fields.put("vatRatePercent", vatRate);
        fields.put("category", category);
        return fields;
    }

    private static Div evidenceView(FieldEvidence evidence) {
        var badge = new Badge(evidence.certainty().label());
        badge.addThemeVariants(BadgeVariant.SMALL, switch (evidence.certainty()) {
            case HIGH -> BadgeVariant.SUCCESS;
            case MEDIUM -> BadgeVariant.WARNING;
            case LOW -> BadgeVariant.ERROR;
        });
        badge.addClassName("evidence-certainty");
        var view = new Div(badge);
        if (evidence.source() != null) {
            var source = new Span("← „" + evidence.source() + "“");
            source.addClassName("evidence-source");
            source.setTitle("So steht es auf dem Beleg");
            view.add(source);
        }
        view.addClassName("evidence");
        view.addClassName("evidence-" + evidence.certainty().name().toLowerCase(Locale.ROOT));
        view.setId("evidence-" + evidence.field());
        // For people: the AI fills in values, it has no use for what the extraction thought of them
        view.getElement().setAttribute("data-draiv-control", "true");
        return view;
    }

    /** Same as {@link #create()}. */
    public void clear() {
        create();
    }

    /** The invoice shown, {@code null} id for a new one. */
    public Invoice getEdited() {
        return edited;
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
