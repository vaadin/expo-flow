package com.example.application.views.dashboard;

import com.example.application.data.service.dashboard.Metric;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.text.DecimalFormat;


public class Highlight extends VerticalLayout {

    private final DecimalFormat percentageFormatter = new DecimalFormat("##.00");
    private final Span percentageBadge = new Span();
    private final Span valueSpan = new Span();

    public Highlight(String title) {

        H2 h2 = new H2(title);
        h2.addClassNames("font-normal", "m-0", "text-secondary", "text-xs");

        valueSpan.addClassNames("font-semibold", "text-3xl");

        add(h2, valueSpan, percentageBadge);
        addClassName("p-l");
        setPadding(false);
        setSpacing(false);
    }

    public void updateValues(Metric metric){
        var percentage = metric.getChange();
        var value = metric.getValue();
        var unit = metric.getUnit();
        var formatter = metric.getFormatter();

        VaadinIcon icon = VaadinIcon.ARROW_UP;
        String prefix = "";
        String theme = "badge";

        if (percentage == 0) {
            prefix = "±";
        } else if (percentage > 0) {
            prefix = "+";
            theme += " success";
        } else if (percentage < 0) {
            icon = VaadinIcon.ARROW_DOWN;
            theme += " error";
        }

        Icon i = icon.create();
        i.addClassNames("box-border", "p-xs");

        valueSpan.setText(formatter.format(value) + unit);
        percentageBadge.removeAll();
        percentageBadge.add(i, new Span(prefix + percentageFormatter.format(percentage)));
        percentageBadge.getElement().getThemeList().removeAll(percentageBadge.getElement().getThemeList());
        percentageBadge.getElement().getThemeList().add(theme);
    }
}
