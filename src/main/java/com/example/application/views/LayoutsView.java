package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Components")
@Route(value = "layouts", layout = MainLayout.class)
public class LayoutsView extends VerticalLayout {

    public LayoutsView() {
        add(new H1("LayoutsView"));
    }
}
