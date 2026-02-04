package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Java Playground")
@Route(value = "playground")
@Menu(title = "Java Playground", icon = LineAwesomeIconUrl.CODE_SOLID, order = 5)
public class PlaygroundView extends VerticalLayout {

    public PlaygroundView(PersonService service) {

        var grid = new Grid<>(Person.class);
        grid.setItems(service.findAll());
        grid.setColumns("firstName", "lastName", "email");

        add(grid);

    }

}
