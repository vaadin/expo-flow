package com.example.application.ai.invoice;

import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Invoices for the AI Driven UI area: search, save, reset to the demo data, and a broadcast of new
 * invoices to every open view (the phone uploads, the big screen shows it).
 */
@Service
public class InvoiceService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    /** The suppliers of the demo data, each with what it typically bills for. */
    private record Supplier(String name, InvoiceCategory category, VatRate vatRate, int minNet, int maxNet) {
    }

    private static final List<Supplier> SUPPLIERS = List.of(
            new Supplier("Acme GmbH", InvoiceCategory.HARDWARE, VatRate.STANDARD, 300, 4_800),
            new Supplier("Bavaria IT-Systeme GmbH", InvoiceCategory.HARDWARE, VatRate.STANDARD, 150, 2_500),
            new Supplier("Nordlicht Software AG", InvoiceCategory.SOFTWARE, VatRate.STANDARD, 49, 1_900),
            new Supplier("CloudNine Ltd.", InvoiceCategory.SOFTWARE, VatRate.ZERO, 99, 1_200),
            new Supplier("Isarhotel München", InvoiceCategory.TRAVEL, VatRate.REDUCED, 120, 980),
            new Supplier("Pixelwerk Agentur", InvoiceCategory.MARKETING, VatRate.STANDARD, 500, 6_500),
            new Supplier("Müller Bürobedarf KG", InvoiceCategory.OFFICE, VatRate.STANDARD, 25, 650));

    private static final int SEED_SIZE = 50;

    private final InvoiceRepository repository;
    private final List<Consumer<Invoice>> listeners = new CopyOnWriteArrayList<>();

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

    public List<Invoice> find(Specification<Invoice> filter, Sort sort) {
        return repository.findAll(filter, sort);
    }

    /** The ten most recently entered invoices, newest first. */
    public List<Invoice> latest() {
        return repository.findTop10ByOrderByCreatedAtDesc();
    }

    /** The suppliers known so far, for the form's suggestions. */
    public List<String> suppliers() {
        return repository.findAll().stream().map(Invoice::getSupplier).distinct().sorted().toList();
    }

    /** Saves the invoice; a new one is announced to every listener. */
    public Invoice save(Invoice invoice) {
        boolean isNew = invoice.getId() == null;
        var saved = repository.save(invoice);
        if (isNew) {
            listeners.forEach(listener -> listener.accept(saved));
        }
        return saved;
    }

    /** Back to the demo data, e.g. between two trade-show visitors. */
    @Transactional
    public void resetAndSeed() {
        repository.deleteAllInBatch();
        repository.saveAll(demoInvoices());
    }

    /**
     * Calls {@code listener} for every new invoice, from whichever thread saved it: a UI listener
     * must use {@code ui.access}. Remove the registration when the view detaches.
     */
    public Registration addInvoiceListener(Consumer<Invoice> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
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
        return invoices;
    }
}
