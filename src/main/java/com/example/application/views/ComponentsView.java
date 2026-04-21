package com.example.application.views;

import com.example.application.data.entity.Person;
import com.example.application.service.PersonService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
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
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Menu(title = "Components", icon = LineAwesomeIconUrl.CUBES_SOLID, order = 1)
@PageTitle("Components")
@Route(value = "components")
@RouteAlias(value = "")
public class ComponentsView extends VerticalLayout {

    private final List<Person> people;
    private Disposable subscription;

    public ComponentsView(PersonService service) {
        addClassName("components-view");

        people = service.findAll();

        addChart();
        addButtons();
        addRadioButtonGroup();
        addLoginForm();
        addDateTimePicker();
        addCheckBoxGroup();
        addGrid();
        addComboBox();
        addMessageList();
        addTextField();
        addUpload();
        addMultiSelectComboBox();
        addTabs();
        addMap();
        addStepper();
        addRichTextEditor();
        addSlider();
        addBadges();
        addProgressBar();
    }

    private void addProgressBar() {
        var layout = new VerticalLayout();
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        layout.add(new H4("Progress Bar"), progressBar);
        layout.setWidth("100%");

        addComponentToGrid(layout, "col-span-2", "wide");
    }

    private void addBadges() {

        Badge pending = new Badge("Pending");

        Badge confirmed = new Badge("Confirmed");
        confirmed.addThemeVariants(BadgeVariant.SUCCESS);

        Badge warning = new Badge("Warning");
        warning.addThemeVariants(BadgeVariant.WARNING);

        Badge denied = new Badge("Denied");
        denied.addThemeVariants(BadgeVariant.ERROR);

        var layout = new VerticalLayout(pending, confirmed, warning, denied);
        layout.setAlignItems(Alignment.CENTER);
        addComponentToGrid(layout);

    }

    private void addSlider() {

        var layout = new VerticalLayout();

        Slider slider = new Slider("Volume");
        slider.setValue(50.0);
        layout.add(slider);

        RangeSlider rangeSlider = new RangeSlider("Price range", 0, 1000);
        rangeSlider.setValue(new RangeSliderValue(200, 800));
        layout.add(rangeSlider);

        addComponentToGrid(layout);
    }

    private void addStepper() {
       var formLayout = new FormLayout();

        IntegerField adultsField = new IntegerField();
        adultsField.setValue(2);
        adultsField.setStepButtonsVisible(true);
        adultsField.setMin(0);
        adultsField.setMax(9);
        formLayout.addFormItem(adultsField, "Adults");

        IntegerField childrenField = new IntegerField();
        childrenField.setValue(2);
        childrenField.setStepButtonsVisible(true);
        childrenField.setMin(0);
        childrenField.setMax(9);

        Div children = new Div("Children");
        Div childrenExplainer = new Div();
        childrenExplainer.setText("Age 2-12");
        childrenExplainer.getStyle().set("font-size", "0.75em");
        childrenExplainer.getStyle().set("position", "relative");

        formLayout.addFormItem(childrenField, new Div(children, childrenExplainer));

        IntegerField infantsField = new IntegerField();
        infantsField.setValue(1);
        infantsField.setStepButtonsVisible(true);
        infantsField.setMin(0);
        infantsField.setMax(9);
        formLayout.addFormItem(infantsField, "Infants");
        formLayout.setWidth("100%");
        formLayout.getStyle().set("position", "relative");

        addComponentToGrid(formLayout, "tall");
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

        addComponentToGrid(dateTimePicker, "col-span-2", "wide");
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

        addComponentToGrid(grid, "col-span-2", "tall", "wide");
    }

    private void addComboBox() {
        var comboBox = new ComboBox<>("People", people);

        comboBox.setItemLabelGenerator(person -> person.getFirstName() + " " + person.getLastName());
        comboBox.setValue(people.get(0));

        addComponentToGrid(comboBox);
    }

    private void addChart() {
        var chart = new Chart(ChartType.LINE);
        var configuration = chart.getConfiguration();
        var xAxis = configuration.getxAxis();
        var yAxis = configuration.getyAxis();
        var series = new DataSeries();
        var rangeSelector = new RangeSelector();
        var plotOptions = new PlotOptionsLine();

        configuration.getChart().setStyledMode(true);
        configuration.setTitle("Developer Productivity");
        configuration.setSeries(series);

        xAxis.setType(AxisType.DATETIME);
        yAxis.setTitle("Productivity (kLOC/h)");

        plotOptions.setMarker(new Marker(false));

        series.setPlotOptions(plotOptions);
        series.setName("Productivity");

        rangeSelector.setSelected(1);
        configuration.setRangeSelector(rangeSelector);

        addAttachListener(e -> {
            subscription = getDeveloperProductivity().subscribe(dataPoint -> {
                e.getUI().access(() -> {
                    series.add(new DataSeriesItem(dataPoint.time(), dataPoint.value()), true, series.getData().size() > 50);
                });
            });
        });

        addDetachListener(e -> {
            subscription.dispose();
        });



        addComponentToGrid(chart, "col-span-2", "tall", "wide");
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
        messageInput.addSubmitListener(event -> messageList.addItem(
                new MessageListItem(event.getValue(),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC), "User")));

        var layout = new VerticalLayout();
        layout.addAndExpand(messageList);
        layout.add(messageInput);
        layout.setSizeFull();

        addComponentToGrid(layout, "col-span-2", "tall", "wide");
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

        addComponentToGrid(multiSelectComboBox);
    }

    private void addTabs() {
        var tabs = new Tabs();

        tabs.add(
                new Tab("Details"),
                new Tab("Payment"),
                new Tab("Shipping")
        );

        addComponentToGrid(tabs);
    }

    private void addMap() {
        var map = new Map();

        map.setHeight("100%");
        map.setCenter(new Coordinate(22.2983171, 60.4515391));
        map.setZoom(4);
        map.getFeatureLayer().addFeature(new MarkerFeature(new Coordinate(22.2983171, 60.4515391)));

        addComponentToGrid(map, "col-span-2", "tall", "wide");
    }

    private void addRichTextEditor() {
        var richTextEditor = new RichTextEditor();

        richTextEditor.setHeight("100%");
        richTextEditor.setValue("<h1>Rich Text Editor</h1><p>This is a rich text editor</p>");

        addComponentToGrid(richTextEditor, "col-span-2", "tall", "wide");
    }

    private void addComponentToGrid(Component component, String... classNames) {
        var wrapper = new Div();
        wrapper.addClassName("component");
        wrapper.addClassName("aura-surface");
        wrapper.add(component);
        wrapper.addClassNames(classNames);

        add(wrapper);
    }

    record DataPoint(Instant time, double value) {}

    // Return a Flux with random data points that grow over time, similar to stock data
    public Flux<DataPoint> getDeveloperProductivity() {
        AtomicLong counter = new AtomicLong();
        AtomicReference<LocalDate> currentDay = new AtomicReference<>(LocalDate.of(2023, 1, 1));

        return Flux.interval(Duration.ofMillis(1000))
                .map(tick -> {
                    double value = Math.pow(counter.incrementAndGet(), 1.02) + (20 * Math.sin(counter.get() / 50.0)) + (Math.random() * 5);
                    Instant instant = currentDay.get().atStartOfDay(ZoneId.systemDefault()).toInstant();
                    currentDay.set(currentDay.get().plusDays(1));
                    return new DataPoint(instant, value);
                });
    }

}
