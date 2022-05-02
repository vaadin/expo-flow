package com.example.application.views.crud;

import com.example.application.data.entity.Person;
import com.example.application.data.service.PersonService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.data.domain.PageRequest;

@PageTitle("CRUD")
@Route(value = "crud", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class CRUDView extends SplitLayout {


    private Grid<Person> grid = new Grid<>(Person.class, false);

    private TextField firstName = new TextField("First Name");
    private TextField lastName = new TextField("Last Name");
    private TextField email = new TextField("Email");
    private DatePicker dateOfBirth = new DatePicker("Date Of Birth");
    private TextField country = new TextField("Country");

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Person> binder;

    private Person person;

    private final PersonService personService;

    public CRUDView(PersonService personService) {
        this.personService = personService;
        setSizeFull();
        setSplitterPosition(65);

        createGrid();
        createEditor();
    }

    private void createGrid() {
        grid.addColumn(c -> c.getFirstName() + " " + c.getLastName())
                .setHeader("Name");
        grid.addColumns("dateOfBirth", "email");
        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setItems(query -> personService.list(
                        VaadinSpringDataHelpers.toSpringPageRequest(query))
        );

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            var person = event.getValue();
            if(person != null) {
                populateForm(person);
            } else {
                clearForm();
            }
        });

        addToPrimary(grid);
    }

    private void createEditor() {
        VerticalLayout editorLayout = new VerticalLayout();

        FormLayout formLayout = new FormLayout();

        formLayout.add(firstName, lastName, email, dateOfBirth, country);
        editorLayout.add(formLayout);
        editorLayout.add(createButtonLayout());
        addToSecondary(editorLayout);


        // Bind fields. This is where you'd define e.g. validation rules
        binder = new BeanValidationBinder<>(Person.class);
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.person == null) {
                    this.person = new Person();
                }
                binder.writeBean(this.person);

                personService.update(this.person);
                clearForm();
                refreshGrid();
                Notification.show("Person details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the person details.");
            }
        });
    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        return buttonLayout;
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Person value) {
        this.person = value;
        binder.readBean(this.person);
    }
}
