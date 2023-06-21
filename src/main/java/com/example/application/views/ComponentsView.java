package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
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
import java.util.List;
import java.util.Set;

@PageTitle("Components")
@Route(value = "components", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ComponentsView extends VerticalLayout {

    private final List<Person> people;

    public ComponentsView(PersonService service) {
        addClassName("components-view");

        people = service.findAll();

        addLoginForm();
        addButtons();
        addRadioButtonGroup();
        addDateTimePicker();
        addCheckBoxGroup();
        addGrid();
        addComboBox();
        addChart();
        addMessageList();
        addTextField();
        addUpload();
        addMultiSelectComboBox();
        addTabs();
        addMap();
        addRichTextEditor();
    }

    private void addLoginForm() {
        var loginForm = new LoginForm();

        addComponentToGrid(loginForm, "col-span-2", "tall");
    }

    private void addButtons() {
        var save = new Button("Save");
        var cancel = new Button("Cancel");
        var layout = new HorizontalLayout();

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        layout.add(save, cancel);

        addComponentToGrid(layout);
    }

    private void addRadioButtonGroup() {
        var radioButtonGroup = new RadioButtonGroup<String>();

        radioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioButtonGroup.setItems("Option one", "Option two", "Option three");
        radioButtonGroup.setValue("Option one");

        addComponentToGrid(radioButtonGroup);
    }

    private void addDateTimePicker() {
        var dateTimePicker = new DateTimePicker("Meeting date and time");

        dateTimePicker.setValue(LocalDateTime.now());

        addComponentToGrid(dateTimePicker, "col-span-2");
    }

    private void addCheckBoxGroup() {
        var checkboxGroup = new CheckboxGroup<String>();

        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        checkboxGroup.setItems("Option one", "Option two", "Option three");
        checkboxGroup.setValue(Set.of("Option one", "Option three"));

        addComponentToGrid(checkboxGroup);
    }

    private void addGrid() {
        var grid = new Grid<>(Person.class);

        grid.setItems(people);
        grid.setColumns("firstName", "lastName", "email", "dateOfBirth");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setHeight("100%");

        people.subList(2, 5).forEach(grid::select);

        addComponentToGrid(grid, "col-span-3", "tall");
    }

    private void addComboBox() {
        var comboBox = new ComboBox<>("People", people);

        comboBox.setItemLabelGenerator(person -> person.getFirstName() + " " + person.getLastName());
        comboBox.setValue(people.get(0));

        addComponentToGrid(comboBox);
    }

    private void addChart() {
        var chart = new Chart(ChartType.PIE);
        var configuration = chart.getConfiguration();

        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("Yes", 10));
        series.add(new DataSeriesItem("No", 20));
        series.add(new DataSeriesItem("Maybe", 5));

        configuration.setSeries(series);
        configuration.getChart().setStyledMode(true);

        addComponentToGrid(chart, "col-span-2", "tall");
    }

    private void addMessageList() {
        var messageList = new MessageList();
        var messageInput = new MessageInput();
        messageList.setItems(
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
        messageInput.setWidth("100%");

        var layout = new VerticalLayout(messageList, messageInput);

        addComponentToGrid(layout, "col-span-2", "tall");
    }

    private void addTextField() {
        TextField textField = new TextField("Search");

        textField.setPrefixComponent(VaadinIcon.SEARCH.create());
        textField.setValue("John Doe");
        textField.setClearButtonVisible(true);

        addComponentToGrid(textField);
    }

    private void addUpload() {
        var upload = new Upload();

        addComponentToGrid(upload);
    }

    private void addMultiSelectComboBox() {
        MultiSelectComboBox<Person> multiSelectComboBox = new MultiSelectComboBox<>("People", people);

        multiSelectComboBox.setWidth("100%");
        multiSelectComboBox.setItemLabelGenerator(person -> person.getFirstName() + " " + person.getLastName());
        multiSelectComboBox.setValue(Set.of(people.get(0), people.get(12)));

        addComponentToGrid(multiSelectComboBox, "col-span-2");
    }

    private void addTabs() {
        var tabs = new Tabs();

        tabs.add(
                new Tab("Details"),
                new Tab("Payment"),
                new Tab("Shipping")
        );

        addComponentToGrid(tabs, "col-span-2");
    }

    private void addMap() {
        var map = new Map();

        map.setHeight("100%");
        map.setCenter(new Coordinate(22.2983171, 60.4515391));
        map.setZoom(4);
        map.getFeatureLayer().addFeature(new MarkerFeature(new Coordinate(22.2983171, 60.4515391)));

        addComponentToGrid(map, "col-span-2", "tall");
    }

    private void addRichTextEditor() {
        var richTextEditor = new RichTextEditor();

        richTextEditor.setHeight("100%");
        richTextEditor.setValue("<h1>Rich Text Editor</h1><p>This is a rich text editor</p>");

        addComponentToGrid(richTextEditor, "col-span-2", "tall");
    }

    private void addComponentToGrid(Component component, String... classNames) {
        var wrapper = new Div();
        wrapper.addClassName("component");
        wrapper.add(component);
        wrapper.addClassNames(classNames);

        add(wrapper);
    }
}
