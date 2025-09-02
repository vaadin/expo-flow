/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.application.views;

import com.example.application.data.entity.TShirtOrder;
import com.example.application.data.repository.TShirtOrderRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Route
public class ListOrdersView extends VerticalLayout {
    
    private final TShirtOrderRepository repo;
    final Grid<TShirtOrder> orders = new Grid<>(TShirtOrder.class);
    
    public ListOrdersView(TShirtOrderRepository repo) {
        this.repo = repo;
        // Build the layout
        H1 heading = new H1("List of submitted orders");
        Button update = new Button(VaadinIcon.REFRESH.create());
        RouterLink orderView = new RouterLink("Submit new order", TShirtView.class);
        add(heading, update, orders, orderView);
        
        orders.setColumns("name", "email", "shirtSize", "points");
        orders.addComponentColumn(order -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addClickListener(e -> {
                repo.delete(order);
                listOrders();
            });
            return deleteBtn;
        });
        listOrders();
        
        update.addClickListener(e -> listOrders());

        add(new Button("Export", e -> {
            UI.getCurrent().getPage().open("/exportorders");
        }));
    }

    public void listOrders() {
        orders.setItems(repo.findAll());
    }


    @RestController
    public static class OrderExportService {

        @Autowired
        private TShirtOrderRepository repository;

        @GetMapping("/exportorders")
        public ResponseEntity<String> export() {

            String data = repository.findAll()
                    .stream()
                    .map(o -> String.format("%s;%s;%s", o.getName(),o.getEmail(), o.getShirtSize()))
                    .collect(Collectors.joining("\n"));
            return  ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=orderexport.txt")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(data);
        }

    }
}
