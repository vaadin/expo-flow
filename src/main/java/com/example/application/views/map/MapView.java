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
    private final Map map = new Map();

    public MapView() {
        setSizeFull();
        setPadding(false);
        map.addThemeName("borderless");

        View view = map.getView();
        view.setCenter(new Coordinate(-84.3982603, 33.7591776));
        view.setZoom(16);

        var conferenceCenter = new MarkerFeature(new Coordinate(6.880651017337574, 50.80061514826724));

        map.getFeatureLayer().addFeature(conferenceCenter);

        map.addClickEventListener(e->
                conferenceCenter.setCoordinates(e.getCoordinate()) );

        addAndExpand(map);
    }
}
