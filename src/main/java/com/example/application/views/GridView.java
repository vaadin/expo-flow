package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Grid")
@Route(value = "grid")
@Menu(title = "Grid", icon = LineAwesomeIconUrl.TABLE_SOLID, order = 3)
public class GridView extends VerticalLayout {

    public GridView(PersonService personService) {
        var grid = new Grid<>(Person.class, false);

        grid.addColumn(c -> c.getFirstName() + " " + c.getLastName())
                .setHeader("Name");
        grid.addColumns("dateOfBirth", "email");
        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.setSizeFull();

        grid.setItemsPageable(personService::unfilteredList);

        setSizeFull();
        add(grid);
    }
}
