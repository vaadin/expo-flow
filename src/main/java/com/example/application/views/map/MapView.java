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

    private Map map = new Map();

    public MapView() {
        setSizeFull();
        setPadding(false);
        map.addThemeName("borderless");

        View view = map.getView();
        view.setCenter(Coordinate.fromLonLat(-100, 38));
        view.setZoom(4);

        var devNexus = new MarkerFeature(Coordinate.fromLonLat(-84.38, 33.75));
        map.getFeatureLayer().addFeature(devNexus);

        addAndExpand(map);
    }
}
