package com.example.application.views;

import com.example.application.components.SwitchComponent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Menu(title = "External-Component", order = 7, icon = LineAwesomeIconUrl.DRUMSTICK_BITE_SOLID)
@PageTitle("External-Component")
@Route("external-component")
public class ExternalComponentView extends VerticalLayout {

    public ExternalComponentView() {
        // Create the switch component
        var switchComponent = new SwitchComponent();
        // Add value change listener
        switchComponent.addValueChangeListener(value -> {
            Notification.show("Value changed to " + value.toString());
        });

        add(switchComponent);
        setSpacing(true);
        setPadding(true);
    }
}
