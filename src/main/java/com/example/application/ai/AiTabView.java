package com.example.application.ai;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** A tab of the AI Driven UI area: a title and one sentence on what it shows, then its content. */
abstract class AiTabView extends VerticalLayout {

    AiTabView(String title, String story) {
        var heading = new H2(title);
        var description = new Paragraph(story);
        description.addClassName("ai-tab-story");
        add(heading, description);
        setPadding(false);
    }
}
