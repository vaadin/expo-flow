package com.example.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Components")
@Route(value = "layouts", layout = MainLayout.class)
public class LayoutsView extends VerticalLayout {

    public LayoutsView() {
        add(new H2("Vertical Layout"));
        add(new VerticalLayout(
                new Button("Button 1"),
                new Button("Button 2"),
                new Button("Button 3")
        ));

        add(new H2("Horizontal Layout"));
        add(new HorizontalLayout(
                new Button("Button 1"),
                new Button("Button 2"),
                new Button("Button 3")
        ));

        add(new H2("Combining layouts"));
        add(new VerticalLayout(

                new TextField("Name"),
                new EmailField("Email"),
                new DatePicker("Date of birth"),

                new HorizontalLayout(
                        new Button("Save"),
                        new Button("Cancel")
                )
        ));
    }
}
