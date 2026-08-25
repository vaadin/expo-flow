package com.example.application.views.quiz;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@PageTitle("Underwater World Quiz")
@Menu(title = "Ocean Quiz", icon = LineAwesomeIconUrl.QUESTION_CIRCLE)
@Route("quiz")
public class QuizView extends VerticalLayout {

    private final QuizSubmitForm submitForm;
    private int questionsAnswered, points = 0;

    public QuizView(QuizSubmitForm submitForm) {
        // Attach CSS class name for some fancy styling
        setClassName(getClass().getSimpleName().toLowerCase());
        this.submitForm = submitForm;

        add(new H1("Dive, Discover, Vaadin"));

        add(new Markdown("""
                **Choose wisely, you can only choose once per question!**
                
                *Hint, you are allowed to sneak in the source code with IDE, 
                the lazy Java developer, might have hardcoded the answers to view code 🤓*
                """));

        // Q1 — Select
        add(new LargestOceanAnimalCard());
        // Q2 — Data Grid (multi-select rows)
        add(new CephalopodsCard());
        // Q3 - Map
        add(new GreatBarrierReefCard());
        // Q4 - Chart
        add(new PacificOceanCard());

    }

    private void checkIfQuizCompleted() {
        if (questionsAnswered == 4) {
            submitForm.showResults(points);
        }
    }

    class QuestionCard extends Card {
        public QuestionCard(String caption, int questionNumber) {
            setMedia(new Image("/images/underwater/q%s.png".formatted(questionNumber), "Featured image"));
            setWidthFull();
            setMaxWidth(1000, Unit.PIXELS);
            setTitle(caption);
            addThemeVariants(CardVariant.LUMO_COVER_MEDIA);
            getStyle().set("--vaadin-card-media-aspect-ratio", "4 / 1");
        }

        public void markAnswered(boolean correct) {
            setEnabled(false);
            questionsAnswered++;
            if (correct) {
                points++;
                LottieComponent confetti = new LottieComponent("/confetti.lottie",
                        true, false, null, null);
                confetti.makeFullOverlay();
                add(confetti);
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

    private class LargestOceanAnimalCard extends QuestionCard {
        public LargestOceanAnimalCard() {
            super("Select: What is the largest animal in the ocean?", 1);
            var animalSelect = new Select<String>();
            animalSelect.setLabel("Choose");
            animalSelect.setItems("Great White Shark", "Blue Whale", "Giant Squid", "Whale Shark");
            animalSelect.setPlaceholder("Choose…");
            animalSelect.addValueChangeListener(e -> {
                markAnswered("Blue Whale".equals(e.getValue()));
                add(new Paragraph("The blue whale is the largest animal known to have ever existed, reaching up to 30 metres long and 190 tonnes. It feeds almost exclusively on tiny krill."));
            });
            add(animalSelect);
        }
    }

    private class CephalopodsCard extends QuestionCard {
        public CephalopodsCard() {
            super("Data Grid: Select all creatures that are cephalopods:", 2);
            var creatureGrid = new Grid<>(SeaCreature.class);
            creatureGrid.setItems(SeaCreature.values());
            creatureGrid.setColumns("name", "depth");
            creatureGrid.setSelectionMode(Grid.SelectionMode.MULTI);
            creatureGrid.setAllRowsVisible(true);
            creatureGrid.asMultiSelect().addSelectionListener(e -> {
                if (!e.getAddedSelection().isEmpty()) {
                    Notification.show("Great choice! Any more options?");
                }
            });
            add(creatureGrid, new Button("Lock my answer") {{
                addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                addClickListener(e -> {
                    Set<SeaCreature> correctChoices = Arrays.stream(SeaCreature.values())
                            .filter(SeaCreature::isCorrect)
                            .collect(Collectors.toSet());
                    Set<SeaCreature> selection = creatureGrid.asMultiSelect().getSelectedItems();
                    markAnswered(correctChoices.equals(selection));
                    creatureGrid.addColumn("type");
                    // add a custom column to show if the answer was correct with icons
                    creatureGrid.addComponentColumn(creature -> {
                        if (creature.isCorrect()) {
                            return VaadinIcon.CHECK.create();
                        } else {
                            return VaadinIcon.MINUS.create();
                        }
                    }).setHeader("Is a cephalopod?");
                });
            }});
        }
    }

    private class GreatBarrierReefCard extends QuestionCard {
        public GreatBarrierReefCard() {
            super("Map: Click the marker on the Great Barrier Reef", 3);
            var map = new com.vaadin.flow.component.map.Map();
            map.setWidthFull();
            map.setHeight("320px");
            map.setCenter(new Coordinate(160.0, -5.0)); // Pacific / Oceania region (lon, lat)
            map.setZoom(2.5);

            // Using a simplified background layer with XYZSource from Stadia Maps,
            // so that the reef is not shown as text 😎
            XYZSource.Options opt = new XYZSource.Options();
            // Use @2x for HiDPI since {r} isn't supported by Vaadin Map
            opt.setUrl("https://tiles.stadiamaps.com/tiles/stamen_toner_background/{z}/{x}/{y}@2x.png");
            opt.setAttributions(java.util.List.of(
                    "© Stadia Maps", "© Stamen Design", "© OpenMapTiles", "© OpenStreetMap contributors"
            ));
            TileLayer bg = new TileLayer();
            bg.setSource(new XYZSource(opt));
            map.setBackgroundLayer(bg);

            MarkerFeature greatBarrierReef = new MarkerFeature(new Coordinate(147.7, -18.3));
            MarkerFeature marianaTrench = new MarkerFeature(new Coordinate(142.2, 11.35));
            MarkerFeature galapagos = new MarkerFeature(new Coordinate(-90.5, -0.5));
            MarkerFeature hawaii = new MarkerFeature(new Coordinate(-157.5, 21.3));
            map.getFeatureLayer().addFeature(greatBarrierReef);
            map.getFeatureLayer().addFeature(marianaTrench);
            map.getFeatureLayer().addFeature(galapagos);
            map.getFeatureLayer().addFeature(hawaii);

            map.addFeatureClickListener(e -> {
                var f = e.getFeature();
                markAnswered(f == greatBarrierReef);
                // Add marker texts to show the map names
                greatBarrierReef.setText("Great Barrier Reef");
                marianaTrench.setText("Mariana Trench");
                galapagos.setText("Galápagos Islands");
                hawaii.setText("Hawaii");
            });
            add(map);
            //setTitle(map);
        }
    }

    private class PacificOceanCard extends QuestionCard {
        public PacificOceanCard() {
            super("Pie Chart: Which slice represents the Pacific Ocean? (Click a slice)", 4);
            setSubtitle(new Span("The five oceans of the world, by their surface area."));
            Chart pieChart = new Chart(ChartType.PIE);
            pieChart.getConfiguration().getChart().setBackgroundColor(new SolidColor("transparent"));
            Configuration config = pieChart.getConfiguration();
            config.setTitle("Oceans by Size");

            PlotOptionsPie plot = new PlotOptionsPie();
            plot.setShowInLegend(false); // Hide legend for as it would be too easy
            config.setPlotOptions(plot);

            DataSeriesItem pacific = new DataSeriesItem("???", 46);
            DataSeriesItem atlantic = new DataSeriesItem("???", 24);
            DataSeriesItem indian = new DataSeriesItem("???", 20);
            DataSeriesItem southern = new DataSeriesItem("???", 6);
            DataSeriesItem arctic = new DataSeriesItem("???", 4);
            DataSeries series = new DataSeries();
            series.add(pacific);
            series.add(atlantic);
            series.add(indian);
            series.add(southern);
            series.add(arctic);
            config.setSeries(series);

            pieChart.addPointClickListener(event -> {
                markAnswered(pacific == event.getItem());
                // Update the chart with correct names
                pacific.setName("Pacific");
                atlantic.setName("Atlantic");
                indian.setName("Indian");
                southern.setName("Southern");
                arctic.setName("Arctic");
                pieChart.drawChart();
            });
            add(pieChart);
        }
    }
}
