package com.example.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Form")
@Route(value = "form", layout = MainLayout.class)
public class FormView extends VerticalLayout {

    private final TextField firstName = new TextField("First Name");
    private final TextField lastName = new TextField("Last Name");
    private final TextField email = new TextField("Email");
    private final DatePicker dateOfBirth = new DatePicker("Date Of Birth");
    private final TextField country = new TextField("Country");

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    public FormView() {
        add(new H1("FormView"));
    }
}
