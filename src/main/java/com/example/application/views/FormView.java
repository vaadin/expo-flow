package com.example.application.views;

import com.example.application.data.entity.Person;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Form")
@Route(value = "form", layout = MainLayout.class)
public class FormView extends VerticalLayout {

    private final TextField firstName = new TextField("First Name");
    private final TextField lastName = new TextField("Last Name");
    private final TextField email = new TextField("Email");
    private final DatePicker dateOfBirth = new DatePicker("Date Of Birth");

    private BeanValidationBinder<Person> binder = new BeanValidationBinder<>(Person.class);

    public FormView() {
        binder.bindInstanceFields(this);

        var save = new Button("Save");
        var cancel = new Button("Cancel");

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> {
            saveAndDisplay();
        });

        cancel.addClickListener(event -> resetForm());

        add(
                firstName,
                lastName,
                email,
                dateOfBirth,
                new HorizontalLayout(save, cancel)
        );

        resetForm();
    }

    private void saveAndDisplay() {
        var person = new Person();

        try {
            binder.writeBean(person);

            var notification = new Notification();
            notification.setDuration(3000);
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            notification.setText("Saved " + person.getFirstName() + " " + person.getLastName());
            notification.open();
        } catch (Exception e) {
            Notification.show("Error saving person");
        }
    }

    private void resetForm() {
        binder.setBean(new Person());
    }


}
