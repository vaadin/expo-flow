package com.example.application.ai;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder until this tab is built, see docs/ai-driven-ui.md. */
@PageTitle("Form Filler")
@Route("ai/form")
public class AiFormFillerView extends AiTabView {

    public AiFormFillerView() {
        super("Form Filler", "Eine Lieferanten-E-Mail ins Formular übertragen: Die AI füllt aus, speichern tut der Mensch.");
    }
}
