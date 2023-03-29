package com.example.application.views.dashboard;


import com.example.application.data.service.dashboard.DashboardService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import reactor.core.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Board {

    private final DashboardService service;
    private final Map<String, Highlight> highlights = new HashMap<>();
    private ComboBox<String> year;
    private VerticalLayout viewEvents;
    private Chart chart;
    private Disposable disposable;


    public DashboardView(DashboardService service) {
        this.service = service;
        setSizeFull();
        addRow(createHighlights());
        addRow(createViewEvents());
    }

    private Component[] createHighlights() {
        List.of(DashboardService.MetricType.values()).forEach(metric ->
                highlights.put(metric.getName(), new Highlight(metric.getName())));

        return highlights.values().toArray(new Highlight[0]);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        var ui = attachEvent.getUI();
        disposable = service.getMetrics().subscribe(metrics -> {
            ui.access(() -> {
                metrics.forEach(metric -> {
                    var highlight = highlights.get(metric.getName());
                    highlight.updateValues(metric);
                });
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        disposable.dispose();
    }

    private Component createViewEvents() {
        // Header
        year = new ComboBox<>();
        year.setItems("2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023");
        year.setValue("2023");
        year.setWidth("100px");
        year.addValueChangeListener(event -> {
            var newChart = createChart();
            viewEvents.replace(chart, newChart);
            chart = newChart;
        });

        HorizontalLayout header = createHeader("Orders", "Cumulative (city/month)");
        header.add(year);

        //Create Chart
        chart = createChart();

        // Add it all together
        viewEvents = new VerticalLayout(header, chart);
        viewEvents.addClassName("p-l");
        viewEvents.setPadding(false);
        viewEvents.setSpacing(false);
        viewEvents.getElement().getThemeList().add("spacing-l");
        return viewEvents;
    }

    private Chart createChart() {
        // Chart
        Chart chart = new Chart(ChartType.AREA);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);

        XAxis xAxis = new XAxis();
        xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        conf.addxAxis(xAxis);

        conf.getyAxis().setTitle("Orders");

        PlotOptionsArea plotOptions = new PlotOptionsArea();
        plotOptions.setPointPlacement(PointPlacement.ON);
        conf.addPlotOptions(plotOptions);

        service.getEventInfo().forEach(eventInfo -> {
            conf.addSeries(new ListSeries(eventInfo.getCity(), eventInfo.getValues().toArray(new Integer[0])));
        });

        return chart;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames("text-xl", "m-0");

        Span span = new Span(subtitle);
        span.addClassNames("text-secondary", "text-xs");

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }
}
