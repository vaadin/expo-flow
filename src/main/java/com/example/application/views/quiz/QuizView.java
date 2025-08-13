package com.example.application.views.quiz;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.XYZSource;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.firitin.util.VStyle;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@PageTitle("Ancient Rome Quiz")
@Menu(title = "Rome Quiz", icon = LineAwesomeIconUrl.QUESTION_CIRCLE)
@Route("quiz")
public class QuizView extends VerticalLayout {

    private final QuizSubmitForm sumbitForm;
    private int questionsAnswered, points = 0;

    public QuizView(QuizSubmitForm sumbitForm) {
        this.sumbitForm = sumbitForm;
        add(new H1("Ancient Rome — JavaZone Vaadin Quiz"));

        add(new Markdown("""
                **Choose wisely, you can only choose once per question!**
                
                *Hint, you are allowed to sneak in the source code with IDE, 
                the lazy Java developer, might have hardcoded the answers to view code 🤓*
                """));

        // Q1 — Select
        add(new QuestionCard("Q1 — Select: Who was the first Roman emperor?") {{
            setMedia(new Image("/images/rome/q1.jpg","Featured image for the question 1"));
            var emperorSelect = new Select<String>();
            emperorSelect.setLabel("Choose");
            emperorSelect.setItems("Julius Caesar", "Augustus", "Nero", "Trajan");
            emperorSelect.setPlaceholder("Choose…");
            emperorSelect.addValueChangeListener(e -> {
                markAnswered("Augustus".equals(e.getValue()));
                add(new Paragraph("Augustus was the first Roman emperor, reigning from 27 BC until his death in AD 14. He was the founder of the Roman Principate and is considered one of the most effective leaders in history."));
            });
            add(emperorSelect);
        }});

        // Q2 — Data Grid (multi-select rows)
        add(new QuestionCard("Q2 — Data Grid: Select all battles that were part of the Punic Wars:") {{
            setMedia(new Image("/images/rome/q2.jpg", "Featured image for the question 2"));
            var battleGrid = new Grid<>(Battle.class);
            battleGrid.setItems(Battle.values());
            battleGrid.setColumns("name", "start");
            battleGrid.setSelectionMode(Grid.SelectionMode.MULTI);
            battleGrid.setAllRowsVisible(true);
            battleGrid.asMultiSelect().addSelectionListener(e -> {
                if (!e.getAddedSelection().isEmpty()) {
                    Notification.show("Great choice! Any more options?");
                }
            });
            add(battleGrid, new Button("Lock my answer", e -> {
                Set<Battle> correctChoises = Arrays.asList(Battle.values()).stream()
                        .filter(Battle::isCorrect)
                        .collect(Collectors.toSet());
                Set<Battle> selection = battleGrid.asMultiSelect().getSelectedItems();
                markAnswered(correctChoises.equals(selection));
                battleGrid.addColumn("war");
                // add a custom column to show if the answer was correct with icons
                battleGrid.addComponentColumn(battle -> {
                    if (battle.isCorrect()) {
                        return VaadinIcon.CHECK.create();
                    } else {
                        return VaadinIcon.MINUS.create();
                    }
                }).setHeader("Part of Punic Wars?");
            }));
        }});

        add(new QuestionCard("Q3 — Map: Click the marker dropped to Rome") {{
            setMedia(new Image("/images/rome/q3.jpg", "Featured image for the question 3"));
            var map = new com.vaadin.flow.component.map.Map();
            map.setWidthFull();
            map.setHeight("320px");
            map.setCenter(new Coordinate(16.0, 38.0)); // approx central Mediterranean (lon, lat)
            map.setZoom(4.5);

            // Using a simplified background layer with XYZSource from Stadia Maps,
            // so that Rome is not shown as text 😎
            XYZSource.Options opt = new XYZSource.Options();
            // Use @2x for HiDPI since {r} isn't supported by Vaadin Map
            opt.setUrl("https://tiles.stadiamaps.com/tiles/stamen_toner_background/{z}/{x}/{y}@2x.png");
            opt.setAttributions(java.util.List.of(
                    "© Stadia Maps", "© Stamen Design", "© OpenMapTiles", "© OpenStreetMap contributors"
            ));
            TileLayer bg = new TileLayer();
            bg.setSource(new XYZSource(opt));
            map.setBackgroundLayer(bg);

            MarkerFeature rome = new MarkerFeature(new Coordinate(12.4964, 41.9028));
            MarkerFeature carthage = new MarkerFeature(new Coordinate(10.3230, 36.8521));
            MarkerFeature athens = new MarkerFeature(new Coordinate(23.7275, 37.9838));
            MarkerFeature alexandria = new MarkerFeature(new Coordinate(29.9187, 31.2001));
            map.getFeatureLayer().addFeature(rome);
            map.getFeatureLayer().addFeature(carthage);
            map.getFeatureLayer().addFeature(athens);
            map.getFeatureLayer().addFeature(alexandria);

            map.addFeatureClickListener(e -> {
                var f = e.getFeature();
                markAnswered(f == rome);
                // Add marker texts to show the map names
                rome.setText("Rome");
                carthage.setText("Carthage");
                athens.setText("Athens");
                alexandria.setText("Alexandria");
            });
            add(map);
        }});

        add(new QuestionCard("Q4 — Pie Chart: Which slice represents Plebeians? (Click a slice)") {{
            setMedia(new Image("/images/rome/q4.jpg", "Featured image for the question 4"));
            setSubtitle(new Span("Roman Social Classes, Patricians, Equestrians, Plebeians, Slaves & Freedmen, by their size."));
            Chart pieChart = new Chart(ChartType.PIE);
            pieChart.getConfiguration().getChart().setBackgroundColor(new SolidColor("transparent"));
            Configuration config = pieChart.getConfiguration();
            config.setTitle("Roman Social Classes");

            PlotOptionsPie plot = new PlotOptionsPie();
            plot.setShowInLegend(false); // Hide legend for as it would be too easy
            config.setPlotOptions(plot);

            DataSeriesItem patricians = new DataSeriesItem("???", 5);
            DataSeriesItem equestrians = new DataSeriesItem("???", 10);
            DataSeriesItem plebeians = new DataSeriesItem("???", 75);
            DataSeriesItem slavesAndFreedmen = new DataSeriesItem("???", 10);
            DataSeries series = new DataSeries();
            series.add(patricians);
            series.add(equestrians);
            series.add(plebeians);
            series.add(slavesAndFreedmen);
            config.setSeries(series);

            pieChart.addPointClickListener(event -> {
                markAnswered(plebeians == event.getItem());
                // Update the chart with correct names
                plebeians.setName("Plebeians");
                patricians.setName("Patricians");
                equestrians.setName("Equestrians");
                slavesAndFreedmen.setName("Slaves & Freedmen");
                pieChart.drawChart();
            });
            add(pieChart);
        }});

        new VStyle() {{
            set("filter", "grayscale(50%)");
        }}.apply(this, "img");

    }

    private void checkIfQuizCompleted() {
        if (questionsAnswered == 4) {
            sumbitForm.showResults(points);
        }
    }

    public enum Battle {
        Cannae("Cannae", "216 BC","Second Punic War", true),
        Alesia("Alesia", "52 BC","Gallic Wars", false),
        Zama("Zama", "202 BC", "Second Punic War", true),
        Actium("Actium", "32 BC", "Final War of the Roman Republic", false),
        Mylae("Mylae", "260 BC", "First Punic War", true);

        private final String name;
        private final String start;
        private final String war;
        private final boolean correct;

        Battle(String name, String start, String war, boolean correct) {
            this.name = name;
            this.start = start;
            this.war = war;
            this.correct = correct;
        }

        public String getName() {
            return name;
        }

        public String getStart() {
            return start;
        }

        public String getWar() {
            return war;
        }

        public boolean isCorrect() {
            return correct;
        }
    }

    class QuestionCard extends Card {
        public QuestionCard(String caption) {
            setWidthFull();
            setMaxWidth(1000, Unit.PIXELS);
            setTitle(caption);
        }

        public QuestionCard(Component content) {
            this("Q1 — Select: Who was the first Roman emperor?");
            add(content);
        }

        public void markAnswered(boolean correct) {
            setEnabled(false);
            questionsAnswered++;
            if (correct) {
                points++;
                Notification.show("Correct answer! You earned a point. %s/%s questions answered."
                        .formatted(questionsAnswered, 4));
                getStyle().setBorder("2px solid green");
            } else {
                Notification.show("Bummer, wrong answer. No points earned. Maybe check the source code now?");
                getStyle().setBorder("2px solid red");
            }
            checkIfQuizCompleted();
        }
    }
}
