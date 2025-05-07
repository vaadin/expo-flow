package com.example.application.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Playground")
@Route(value = "playground")
@Menu(title = "Java Playground", icon = LineAwesomeIconUrl.CODE_SOLID, order = 5)
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
