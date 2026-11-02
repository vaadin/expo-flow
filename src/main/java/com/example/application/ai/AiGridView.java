package com.example.application.ai;

import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/** Placeholder until this tab is built, see docs/ai-driven-ui.md. */
@PageTitle("AI Grid")
@Route("ai/grid")
@Menu(title = "AI Driven UI", icon = LineAwesomeIconUrl.MAGIC_SOLID, order = 0.5)
public class AiGridView extends AiTabView {

    public AiGridView() {
        super("AI Grid", "Rechnungen finden, ohne die Filter selbst zu bedienen: Die AI setzt die Filter über dem Grid.");
    }
}
