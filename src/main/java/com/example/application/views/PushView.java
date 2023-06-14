package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.data.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import reactor.core.Disposable;

import java.util.ArrayList;

@PageTitle("Push")
@Route(value = "push", layout = MainLayout.class)
public class PushView extends VerticalLayout {

    private Disposable subscription;

    public PushView(PersonService service) {
        var people = new ArrayList<Person>();
        var grid = new Grid<>(Person.class, false);
        grid.setColumns("firstName", "lastName", "email");

        add(grid);

        addAttachListener(e -> {
            subscription = service.getPersonStream().subscribe(person -> {
                e.getUI().access(() -> {
                    people.add(person);
                    grid.setItems(people);
                });
            });
        });

        addDetachListener(e -> {
            subscription.dispose();
        });
    }
}
