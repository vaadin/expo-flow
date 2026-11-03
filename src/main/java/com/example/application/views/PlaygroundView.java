package com.example.application.views;

import com.example.application.data.repository.PersonRepository;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Java Playground")
@Route(value = "playground")
@Menu(title = "Java Playground", icon = LineAwesomeIconUrl.CODE_SOLID, order = 5)
public class PlaygroundView extends VerticalLayout {

    public PlaygroundView(PersonRepository repository) {
        var name = new TextField("Your name");
        var button = new Button("Click me");
        button.addClickShortcut(Key.ENTER);

        button.addClickListener(e -> {
            add(new Paragraph("Hello, " + name.getValue()));
        });

        add(name, button);
    }
}
