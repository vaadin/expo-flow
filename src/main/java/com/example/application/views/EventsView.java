package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Events")
@Route(value = "events", layout = MainLayout.class)
public class EventsView extends VerticalLayout {


    public EventsView(PersonService service) {
        var names = service.findAll().stream().map(Person::getFirstName).toList();

        addButtonExample();
        addDatePickerExample();
        addComboBoxExample(names);
    }

    private void addButtonExample() {
        var button = new Button("Click me");
        var message = new Span();

        button.addClickListener(e -> {
            message.setText("Clicked!");
        });

        var layout = new HorizontalLayout(button, message);
        layout.setAlignItems(Alignment.BASELINE);
        add(layout);
    }

    private void addDatePickerExample() {
        var datePicker = new DatePicker("Select date");
        var message = new Span();

        datePicker.addValueChangeListener(e -> {
            message.setText("Date: " + e.getValue());
        });

        var layout = new HorizontalLayout(datePicker, message);
        layout.setAlignItems(Alignment.BASELINE);
        add(layout);
    }

    private void addComboBoxExample(List<String> names) {
        var comboBox = new ComboBox<String>("Select person");
        comboBox.setItems(names);

        var message = new Span();

        comboBox.addValueChangeListener(e -> {
            message.setText("Selected: " + e.getValue());
        });

        var layout = new HorizontalLayout(comboBox, message);
        layout.setAlignItems(Alignment.BASELINE);
        add(layout);
    }

}
