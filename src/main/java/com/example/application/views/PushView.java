package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Push")
@Route(value = "push", layout = MainLayout.class)
public class PushView extends VerticalLayout {

    public PushView() {
        add(new H1("PushView"));
    }
}
