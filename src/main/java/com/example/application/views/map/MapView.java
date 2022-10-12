package com.example.application.views.map;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Map")
@Route(value = "map", layout = MainLayout.class)
public class MapView extends VerticalLayout {

    public MapView() {
        setSizeFull();
        setPadding(false);

        Map map = new Map();

        map.addThemeName("borderless");

        View view = map.getView();
        view.setCenter(new Coordinate(-100, 38));
        view.setZoom(4);

        var javaOne = new MarkerFeature(new Coordinate(-115.1391, 36.1716));
        map.getFeatureLayer().addFeature(javaOne);

        addAndExpand(map);
    }
}
