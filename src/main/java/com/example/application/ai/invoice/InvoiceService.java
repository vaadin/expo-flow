package com.example.application.ai.invoice;

import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Invoices for the AI Driven UI area: search, save, delete, reset to the demo data, and a broadcast of
 * every change to the open views (the phone adds an invoice, the big screen shows it).
 */
@Service
public class InvoiceService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    /** The suppliers of the demo data, each with what it typically bills for. */
    private record Supplier(String name, String email, InvoiceCategory category, VatRate vatRate, int minNet,
                            int maxNet) {
    }

    private static final List<Supplier> SUPPLIERS = List.of(
            new Supplier("Acme GmbH", "rechnung@acme.example", InvoiceCategory.HARDWARE, VatRate.STANDARD, 300, 4_800),
            new Supplier("Bavaria IT-Systeme GmbH", "buchhaltung@bavaria-it.example", InvoiceCategory.HARDWARE, VatRate.STANDARD, 150, 2_500),
            new Supplier("Nordlicht Software AG", "billing@nordlicht.example", InvoiceCategory.SOFTWARE, VatRate.STANDARD, 49, 1_900),
            new Supplier("CloudNine Ltd.", "invoices@cloudnine.example", InvoiceCategory.SOFTWARE, VatRate.ZERO, 99, 1_200),
            new Supplier("Isarhotel München", "rezeption@isarhotel.example", InvoiceCategory.TRAVEL, VatRate.REDUCED, 120, 980),
            new Supplier("Pixelwerk Agentur", "hallo@pixelwerk.example", InvoiceCategory.MARKETING, VatRate.STANDARD, 500, 6_500),
            new Supplier("Müller Bürobedarf KG", "service@mueller-buero.example", InvoiceCategory.OFFICE, VatRate.STANDARD, 25, 650));

    private static final int SEED_SIZE = 50;

    private final InvoiceRepository repository;
    private final List<Consumer<InvoiceChange>> listeners = new CopyOnWriteArrayList<>();

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    /** Seeds the demo invoices on startup, unless there already are some. */
    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() == 0) {
            log.info("Generating {} demo invoices", SEED_SIZE);
            repository.saveAll(demoInvoices());
        }
    }

    /** The invoices matching the filter, in the given order. */
    public List<Invoice> find(InvoiceFilter filter, InvoiceSort sort) {
        return repository.findAll(filter.specification()).stream()
                .filter(filter::matchesGross)
                .sorted(sort.comparator())
                .toList();
    }

    /**
     * The gross total per group of the matching invoices, in axis order; groups without invoices are left
     * out.
     */
    public Map<String, BigDecimal> totals(InvoiceFilter filter, InvoiceGrouping grouping) {
        return repository.findAll(filter.specification()).stream()
                .filter(filter::matchesGross)
                .sorted(grouping.order())
                .collect(Collectors.groupingBy(grouping::groupOf, LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getGross, BigDecimal::add)));
    }

    public long count() {
        return repository.count();
    }

    /** The suppliers so far, so the AI can match "Acme" to the right one. */
    public List<String> suppliers() {
        return repository.findAll().stream().map(Invoice::getSupplier).distinct().sorted().toList();
    }

    /** Invoices whose number or supplier contains {@code text}, newest first; all for a blank text. */
    public List<Invoice> search(String text) {
        var invoices = text == null || text.isBlank() ? repository.findAll()
                : repository.findByInvoiceNumberContainingIgnoreCaseOrSupplierContainingIgnoreCase(text.strip(),
                text.strip());
        return invoices.stream().sorted(InvoiceSort.NEWEST.comparator()).toList();
    }

    /** Creates or updates the invoice and tells every listener. */
    public Invoice save(Invoice invoice) {
        boolean isNew = invoice.getId() == null;
        var saved = repository.save(invoice);
        fire(new InvoiceChange(isNew ? InvoiceChange.Kind.CREATED : InvoiceChange.Kind.UPDATED, saved));
        return saved;
    }

    public void delete(Invoice invoice) {
        repository.delete(invoice);
        fire(new InvoiceChange(InvoiceChange.Kind.DELETED, invoice));
    }

    public void markPaid(Collection<Invoice> invoices) {
        invoices.forEach(invoice -> invoice.setStatus(InvoiceStatus.PAID));
        repository.saveAll(invoices).forEach(saved -> fire(new InvoiceChange(InvoiceChange.Kind.UPDATED, saved)));
    }

    public void delete(Collection<Invoice> invoices) {
        repository.deleteAll(invoices);
        invoices.forEach(invoice -> fire(new InvoiceChange(InvoiceChange.Kind.DELETED, invoice)));
    }

    /** Back to the demo data, e.g. between two trade-show visitors. */
    @Transactional
    public void resetAndSeed() {
        repository.deleteAllInBatch();
        repository.saveAll(demoInvoices());
    }

    /**
     * Calls {@code listener} for every invoice created, changed or deleted, from whichever thread did it: a UI
     * listener must use {@code ui.access}. Remove the registration when the view detaches.
     */
    public Registration addInvoiceListener(Consumer<InvoiceChange> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    private void fire(InvoiceChange change) {
        listeners.forEach(listener -> listener.accept(change));
    }

    /**
     * About 50 invoices over the last six months, the same on every run. Invoices past their due
     * date are mostly paid and otherwise overdue; the others are mostly still open.
     */
    private static List<Invoice> demoInvoices() {
        var random = new Random(2026);
        var today = LocalDate.now();
        var invoices = new ArrayList<Invoice>();
        for (int i = 0; i < SEED_SIZE; i++) {
            var supplier = SUPPLIERS.get(i % SUPPLIERS.size());
            var invoice = new Invoice();
            invoice.setInvoiceDate(today.minusDays(random.nextInt(183)));
            invoice.setInvoiceNumber("RE-%d-%03d".formatted(invoice.getInvoiceDate().getYear(), 101 + i));
            invoice.setSupplier(supplier.name());
            invoice.setContactEmail(supplier.email());
            invoice.setDueDate(invoice.getInvoiceDate().plusDays(random.nextBoolean() ? 14 : 30));
            invoice.setNet(BigDecimal.valueOf(supplier.minNet()
                            + random.nextDouble() * (supplier.maxNet() - supplier.minNet()))
                    .setScale(2, RoundingMode.HALF_UP));
            invoice.setVatRate(supplier.vatRate());
            invoice.setCategory(supplier.category());
            boolean due = invoice.getDueDate().isBefore(today);
            invoice.setStatus(due
                    ? (random.nextInt(4) == 0 ? InvoiceStatus.OVERDUE : InvoiceStatus.PAID)
                    : (random.nextInt(4) == 0 ? InvoiceStatus.PAID : InvoiceStatus.OPEN));
            invoice.setCreatedAt(invoice.getInvoiceDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            invoices.add(invoice);
        }
        // The concept's prompt "alle offenen Rechnungen von Acme über 1.000 €" must find something: the
        // three latest Acme invoices are big and still open, whatever the dice said
        invoices.stream().filter(invoice -> invoice.getSupplier().equals("Acme GmbH"))
                .sorted(Comparator.comparing(Invoice::getInvoiceDate).reversed())
                .limit(3)
                .forEach(invoice -> {
                    invoice.setDueDate(today.plusDays(5 + random.nextInt(25)));
                    invoice.setNet(BigDecimal.valueOf(1_200 + random.nextInt(3_000)).setScale(2, RoundingMode.HALF_UP));
                    invoice.setStatus(InvoiceStatus.OPEN);
                });
        return invoices;
    }
}
