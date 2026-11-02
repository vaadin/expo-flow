package com.example.application.ai;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** Placeholder until this tab is built, see docs/ai-driven-ui.md. */
@PageTitle("Makro")
@Route("ai/macros")
public class AiMacroView extends AiTabView {

    public AiMacroView() {
        super("Makro", "Einmal mit AI, danach ohne: Die AI speichert ihre Schritte als Makro, das ohne Modell wieder läuft.");
    }
}
