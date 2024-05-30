package com.example.application.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Playground")
@Route(value = "playground", layout = MainLayout.class)
public class PlaygroundView extends VerticalLayout {


    public PlaygroundView() {
        var name = new TextField();
        var button = new Button("Say hello");

        name.setPlaceholder("Your name");

        button.addClickShortcut(Key.ENTER);
        button.addClickListener(e -> {
            add(new Paragraph("Hello " + name.getValue()));
            name.clear();
        });

        add(new HorizontalLayout(name, button));
    }

}
