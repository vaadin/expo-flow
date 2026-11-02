package com.example.application.ai;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder until this tab is built, see docs/ai-driven-ui.md. */
@PageTitle("AI Dashboard")
@Route("ai/dashboard")
public class AiDashboardView extends AiTabView {

    public AiDashboardView() {
        super("AI Dashboard", "Ausgaben per Satz auswerten: Die AI stellt die Steuerleiste ein, die Charts folgen.");
    }
}
