package com.example.application.views;

import com.example.application.components.SwitchComponent;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Menu(title = "External-Component", order = 7, icon = LineAwesomeIconUrl.DRUMSTICK_BITE_SOLID)
@PageTitle("External-Component")
@Route("external-component")
public class ExternalComponentView extends HorizontalLayout {

    public ExternalComponentView() {
        // Create the switch component
        var switchComponent = new SwitchComponent();
        switchComponent.setValue(false);
        var stateLabel = new Paragraph("State: " + switchComponent.getValue());

        // Add value change listener
        switchComponent.addValueChangeListener(value -> {
            stateLabel.setText("State: " + value);
        });

        add(switchComponent, stateLabel);
        setAlignItems(Alignment.BASELINE);
        setSpacing(true);
        setPadding(true);
    }
}
