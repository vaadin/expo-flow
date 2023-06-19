package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@PageTitle("Components")
@Route(value = "components", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ComponentsView extends VerticalLayout {

    public ComponentsView(PersonService service) {
        addClassName("components-view");

        var people = service.findAll();

        addComponent(new LoginForm(), "col-span-2", "tall");

        addComponent(
                new HorizontalLayout(
                        new Button("Save") {{
                            addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                        }},
                        new Button("Cancel")
                )
        );

        addComponent(new RadioButtonGroup<>() {{
            addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            setItems("Option one", "Option two", "Option three");
            setValue("Option one");
        }});

        addComponent(new DateTimePicker("Meeting date and time") {{
            setValue(LocalDateTime.now());
        }}, "col-span-2");

        addComponent(new CheckboxGroup<>() {{
            addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
            setItems("Option one", "Option two", "Option three");
            setValue(Set.of("Option one", "Option three"));
        }});

        addComponent(new Grid<>(Person.class) {{
            setItems(people);
            setColumns("firstName", "lastName", "email", "dateOfBirth");
            setSelectionMode(Grid.SelectionMode.MULTI);
            setHeight("100%");
            people.subList(2, 5).forEach(this::select);
        }}, "col-span-3", "tall");

        addComponent(new ComboBox<>("People", people) {{
            setItemLabelGenerator(person -> person.getFirstName() + " " + person.getLastName());
            setValue(people.get(0));
        }});

        addComponent(new VerticalLayout(
                        new MessageList() {{
                            setItems(
                                    new MessageListItem(
                                            "Nature does not hurry, yet everything gets accomplished.",
                                            LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC),
                                            "Matt Mambo") {{
                                        setUserColorIndex(1);
                                    }},
                                    new MessageListItem(
                                            "Using your talent, hobby or profession in a way that makes you contribute with something good to this world is truly the way to go.",
                                            LocalDateTime.now().minusMinutes(55).toInstant(ZoneOffset.UTC),
                                            "Lindsey Listy") {{
                                        setUserColorIndex(2);
                                    }}
                            );
                        }},
                        new MessageInput() {{
                            setWidth("100%");
                        }})

                , "col-span-2", "tall");



        addComponent(new TextField("Search") {{
            setPrefixComponent(VaadinIcon.SEARCH.create());
            setValue("John Doe");
            setClearButtonVisible(true);
        }});

        addComponent(new Upload());

        addComponent(new MultiSelectComboBox<>("People", people) {{
            setItems(people);
            setWidth("100%");
            setItemLabelGenerator(person -> person.getFirstName() + " " + person.getLastName());
            setValue(Set.of(people.get(0), people.get(12)));
        }}, "col-span-2");

        addComponent(new Tabs() {{
            add(new Tab("Details"), new Tab("Payment"), new Tab("Shipping"));
        }}, "col-span-2");

        addComponent(new Map() {{
            setHeight("100%");
            setCenter(new Coordinate(22.2983171, 60.4515391));
            setZoom(4);
            getFeatureLayer().addFeature(new MarkerFeature(new Coordinate(22.2983171, 60.4515391)));
        }}, "col-span-2", "tall");

        addComponent(new RichTextEditor() {{
            setHeight("100%");
            setValue("<h1>Rich Text Editor</h1><p>This is a rich text editor</p>");
        }}, "col-span-2", "tall");
    }

    private void addComponent(Component component, String... classNames) {
        var wrapper = new Div();
        wrapper.addClassName("component");
        wrapper.add(component);
        wrapper.addClassNames(classNames);

        add(wrapper);
    }
}
