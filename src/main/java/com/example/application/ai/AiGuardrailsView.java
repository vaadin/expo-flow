package com.example.application.ai;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder until this tab is built, see docs/ai-driven-ui.md. */
@PageTitle("Guardrails")
@Route("ai/guardrails")
public class AiGuardrailsView extends AiTabView {

    public AiGuardrailsView() {
        super("Guardrails", "Die AI darf nur, was die App erlaubt: Status-Feld und Löschen bleiben außer Reichweite.");
    }
}
