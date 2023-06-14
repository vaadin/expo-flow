package com.example.application.views;

import com.example.application.data.service.PersonService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import reactor.core.Disposable;

@PageTitle("Push")
@Route(value = "push", layout = MainLayout.class)
public class PushView extends VerticalLayout {

    private final Paragraph name;
    private Disposable subscription;

    public PushView(PersonService service) {

        name = new Paragraph();
        name.addClassName(LumoUtility.FontSize.LARGE);

        add(new H3("Most recent login:"), name);

        addAttachListener(e -> {
            subscription = service.getLiveVisitors().subscribe(person -> {
                e.getUI().access(() -> {
                    name.setText(person.getFirstName() + " " + person.getLastName());
                });
            });
        });

        addDetachListener(e -> {
            subscription.dispose();
        });
    }
}
