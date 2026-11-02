package com.example.application.ai;

import com.vaadin.draiv.script.Script;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.script.Step;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * One deterministic macro per tab: what the AI does for the tab's example prompt, replayed without a model.
 * The booth staff press "Ohne AI vorführen" when the network or the model lets them down. They are defined
 * here in code, so a deleted or overwritten macro in the store cannot break them, and also put into the
 * {@link ScriptStore} at startup and on reset, so the macro tab lists them.
 */
@Component
public class FallbackMacros implements ApplicationRunner {

    /** Tab 1: "Alle offenen Rechnungen von Acme über 1.000 €, nach Fälligkeit sortiert". */
    public static final Script GRID = new Script("fallback-ai-grid",
            "Offene Acme-Rechnungen über 1.000 €, nach Fälligkeit sortiert.", List.of(), List.of(
            new Step.Navigate("ai/grid"),
            click("grid-reset-button"),
            set("grid-filter-supplier", "Acme"),
            set("grid-filter-statuses", "Offen"),
            set("grid-filter-min-gross", "1000"),
            set("grid-filter-sort", "Fälligkeit, früheste zuerst")));

    /** Tab 2: the example e-mail ({@link AiFormFillerView#EXAMPLE}) in the form, not saved. */
    public static final Script FORM_FILLER = new Script("fallback-form-filler",
            "Überträgt die Beispiel-E-Mail von Acme ins Formular, ohne zu speichern.", List.of(), List.of(
            new Step.Navigate("ai/form"),
            click("invoice-reset-button"),
            set("invoice-number", "RE-2026-114"),
            set("invoice-supplier", "Acme GmbH"),
            set("invoice-date", "2026-10-28"),
            set("invoice-due-date", "2026-11-30"),
            set("invoice-net", "1000"),
            set("invoice-vat-rate", "19 %"),
            set("invoice-category", "Hardware")));

    /** Tab 3: the values of docs/demo-invoices/1-acme-notebooks.pdf in the form, not added. */
    public static final Script UPLOAD = new Script("fallback-upload",
            "Trägt die Demo-Rechnung 1-acme-notebooks.pdf ins Formular ein, ohne sie hinzuzufügen.", List.of(),
            List.of(
            new Step.Navigate("ai/upload"),
            click("invoice-reset-button"),
            set("invoice-number", "RE-2026-131"),
            set("invoice-supplier", "Acme GmbH"),
            set("invoice-date", "2026-10-27"),
            set("invoice-due-date", "2026-11-26"),
            set("invoice-net", "2676.00"),
            set("invoice-vat-rate", "19 %"),
            set("invoice-category", "Hardware"),
            set("invoice-receipt", "1-acme-notebooks.pdf")));

    /** Tab 4: "Ausgaben pro Kategorie als Balken, nur offene aus Q3". */
    public static final Script DASHBOARD = new Script("fallback-dashboard",
            "Offene Ausgaben aus Q3 pro Kategorie als Balken.", List.of(), List.of(
            new Step.Navigate("ai/dashboard"),
            click("dashboard-reset-button"),
            set("dashboard-grouping", "Ausgaben pro Kategorie"),
            set("dashboard-chart-type", "Balken"),
            set("dashboard-statuses", "Offen"),
            set("dashboard-from", "2026-07-01"),
            set("dashboard-to", "2026-09-30")));

    /** Tab 5: what the AI writes for "Lege drei Testrechnungen an und speichere das als Makro". */
    public static final Script MACRO = new Script("fallback-drei-testrechnungen",
            "Legt drei Testrechnungen an.", List.of(), threeTestInvoices());

    /**
     * Tab 6: filter the invoices older than three months and try to mark them paid. The click is
     * refused: a macro is not a person.
     */
    public static final Script GUARDRAILS = new Script("fallback-guardrails",
            "Filtert die alten Rechnungen und versucht, sie als bezahlt zu markieren: die App lehnt ab.",
            List.of(), List.of(
            new Step.Navigate("ai/guardrails"),
            set("guard-filter-to", LocalDate.now().minusMonths(3).toString()),
            click("guard-mark-paid")));

    public static final List<Script> ALL = List.of(GRID, FORM_FILLER, UPLOAD, DASHBOARD, MACRO, GUARDRAILS);

    private final ScriptStore scripts;

    public FallbackMacros(ScriptStore scripts) {
        this.scripts = scripts;
    }

    @Override
    public void run(ApplicationArguments args) {
        reset();
    }

    /** Back to the fallback macros only: the ones the AI wrote are deleted. */
    public void reset() {
        scripts.list().forEach(scripts::delete);
        ALL.forEach(scripts::save);
    }

    private static List<Step> threeTestInvoices() {
        var steps = new ArrayList<Step>();
        steps.add(new Step.Navigate("ai/upload"));
        String[][] invoices = {
                {"TEST-001", "100", "19 %", "Büro"},
                {"TEST-002", "200", "7 %", "Reise"},
                {"TEST-003", "300", "0 %", "Software"}};
        for (var invoice : invoices) {
            steps.add(set("invoice-number", invoice[0]));
            steps.add(set("invoice-supplier", "Testfirma GmbH"));
            steps.add(set("invoice-date", "2026-11-02"));
            steps.add(set("invoice-due-date", "2026-12-02"));
            steps.add(set("invoice-net", invoice[1]));
            steps.add(set("invoice-vat-rate", invoice[2]));
            steps.add(set("invoice-category", invoice[3]));
            steps.add(click("invoice-add-button"));
        }
        return steps;
    }

    private static Step set(String id, String value) {
        return new Step.Set(id, null, value);
    }

    private static Step click(String id) {
        return new Step.Click(id, null);
    }
}
