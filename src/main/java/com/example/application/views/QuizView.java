package com.example.application.views;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Set;


@PageTitle("Ancient Rome Quiz")
@Route("quiz")
public class QuizView extends VerticalLayout {

    private final Select<String> emperorSelect = new Select<>();
    private final Grid<Battle> battleGrid = new Grid<>(Battle.class, false);
    private final Map map = new Map();
    private final Chart pieChart = new Chart(ChartType.PIE);

    private String mapAnswer = null;
    private String pieAnswer = null;

    public QuizView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(new H1("Ancient Rome — Quiz"));

        // Q1 — Select
        var q1 = new VerticalLayout(); q1.setPadding(false);
        q1.add(new H2("Q1 — Select: Who was the first Roman emperor?"));
        emperorSelect.setLabel("Choose");
        emperorSelect.setItems("Julius Caesar", "Augustus", "Nero", "Trajan");
        emperorSelect.setPlaceholder("Choose…");
        emperorSelect.addValueChangeListener(e -> {
           Notification.show("Great! We will see later, if you knew the right answer.");
        });
        q1.add(emperorSelect);
        add(card(q1));

        // Q2 — Data Grid (multi-select rows)
        var q2 = new VerticalLayout(); q2.setPadding(false);
        q2.add(new H2("Q2 — Data Grid: Select all battles that were part of the Punic Wars:"));
        battleGrid.addColumn(Battle::name).setHeader("Battle");
        battleGrid.addColumn(Battle::war).setHeader("Conflict");
        battleGrid.setItems(Battle.data());
        battleGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        battleGrid.setAllRowsVisible(true);
        battleGrid.asMultiSelect().addSelectionListener(e -> {
            if (!e.getAddedSelection().isEmpty()) {
                Notification.show("Great choice! Any more options?");
            }
        });
        q2.add(battleGrid);
        add(card(q2));

        // Q3 — Map (inline SVG, click a dot)
        var q3 = new VerticalLayout(); q3.setPadding(false);
        q3.add(new H2("Q3 — Map: Click the dot for Rome"));
        map.setWidthFull();
        map.setHeight("320px");
        map.setCenter(new Coordinate(16.0, 38.0)); // approx central Mediterranean (lon, lat)
        map.setZoom(4.5);

        // Background: Stamen Toner (background-only) via Stadia Maps
        XYZSource.Options opt = new XYZSource.Options();
// Use @2x for HiDPI since {r} isn't supported by Vaadin Map
        opt.setUrl("https://tiles.stadiamaps.com/tiles/stamen_toner_background/{z}/{x}/{y}@2x.png");
        opt.setAttributions(java.util.List.of(
                "© Stadia Maps", "© Stamen Design", "© OpenMapTiles", "© OpenStreetMap contributors"
        ));
        TileLayer bg = new TileLayer();
        bg.setSource(new XYZSource(opt));
        map.setBackgroundLayer(bg);

        // Markers for key cities
        var idByFeature = new java.util.HashMap<MarkerFeature,String>();

        MarkerFeature rome = new MarkerFeature(new Coordinate(12.4964, 41.9028));
        MarkerFeature carthage = new MarkerFeature(new Coordinate(10.3230, 36.8521));
        MarkerFeature athens = new MarkerFeature(new Coordinate(23.7275, 37.9838));
        MarkerFeature alexandria = new MarkerFeature(new Coordinate(29.9187, 31.2001));

        idByFeature.put(rome, "rome");
        idByFeature.put(carthage, "carthage");
        idByFeature.put(athens, "athens");
        idByFeature.put(alexandria, "alexandria");

        map.getFeatureLayer().addFeature(rome);
        map.getFeatureLayer().addFeature(carthage);
        map.getFeatureLayer().addFeature(athens);
        map.getFeatureLayer().addFeature(alexandria);

        map.addFeatureClickListener(e -> {
            var f = e.getFeature();
            mapAnswer = idByFeature.getOrDefault(f, null);
            Notification.show("Thanks for choosing. Looks promising, but let's see!");
        });
        q3.add(map);
        add(card(q3));

        // Q4 — Pie Chart (inline SVG, click a slice)
        var q4 = new VerticalLayout(); q4.setPadding(false);
        q4.add(new H2("Q4 — Pie Chart: Which slice represents Plebeians? (Click a slice)"));
        Configuration config = pieChart.getConfiguration();
        config.setTitle("Roman Social Classes");

        PlotOptionsPie plot = new PlotOptionsPie();
        plot.setShowInLegend(true);
        config.setPlotOptions(plot);

        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("Patricians", 5));
        series.add(new DataSeriesItem("Equestrians", 10));
        series.add(new DataSeriesItem("Plebeians", 75));
        series.add(new DataSeriesItem("Slaves & Freedmen", 10));
        config.setSeries(series);

        pieChart.addPointClickListener(event -> {
            String name = event.getItem().getName();
            Notification.show("Thanks for choosing. Let's hope it is the right one!");
            pieAnswer = name;
        });

        q4.add(pieChart);
        add(card(q4));

        // Form and actions
        var name = new TextField(null, "Enter your name");
        var email = new TextField(null, "Enter your email for results");
        var submit = new Button("Submit your answers and participate!", e -> {
            int score = 0;
            if ("Augustus".equals(emperorSelect.getValue())) score++;
            if (isGridCorrect()) score++;
            if ("rome".equals(mapAnswer)) score++;
            if ("Plebeians".equals(pieAnswer)) score++;
            Notification.show("Score: "+score+"/4", 3000, Notification.Position.MIDDLE);
        });
        var title = new H2("Ready to submit?");
        var form = new FormLayout();
        form.setAutoResponsive(true);
        form.setLabelsAside(true);
        form.addFormItem(name,"Your Name");
        form.addFormItem(email,"Your Email");
        add(card(new VerticalLayout(title, form, submit)));
    }

    private boolean isGridCorrect() {
        Set<String> correct = Set.of("Cannae","Zama","Mylae");
        var selected = battleGrid.getSelectedItems();
        if (selected.isEmpty()) return false;
        if (selected.size() != 3) return false;
        return selected.stream().allMatch(b -> correct.contains(b.name()));
    }

    private void reset() {
        emperorSelect.clear();
        battleGrid.deselectAll();
        mapAnswer = null; pieAnswer = null;
    }

    private Card card(com.vaadin.flow.component.Component c) {
        var wrap = new Card();
        wrap.setMaxWidth(1000, Unit.PIXELS);
        wrap.setWidth("100%");
        wrap.add(c);
        return wrap;
    }

    public record Battle(int id, String name, String war, boolean correct) {
        static List<Battle> data() {
            return List.of(
                    new Battle(1, "Cannae", "Second Punic War", true),
                    new Battle(2, "Alesia", "Gallic Wars", false),
                    new Battle(3, "Zama", "Second Punic War", true),
                    new Battle(4, "Actium", "Final War of the Roman Republic", false),
                    new Battle(5, "Mylae", "First Punic War", true)
            );
        }
    }
}
