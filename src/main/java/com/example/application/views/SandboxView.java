package com.example.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Sandbox")
@Route(value = "sandbox", layout = MainLayout.class)
public class SandboxView extends VerticalLayout {


    public SandboxView() {
        var name = new TextField("Your name");
        var button = new Button("Say hello");

        button.addClickListener(e -> {
            add(new Paragraph("Hello " + name.getValue()));
        });

        var form = new HorizontalLayout(name, button);
        form.setAlignItems(Alignment.END);


        add(form);
    }

}
