package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Grid")
@Route(value = "grid", layout = MainLayout.class)
public class GridView extends VerticalLayout {

    public GridView() {
        add(new H1("GridView"));
    }
}
