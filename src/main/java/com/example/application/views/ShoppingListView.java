package com.example.application.views;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Shopping List")
@Menu(title = "Shopping List", icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID, order = 6)
@Route(value = "shopping-list")
public class ShoppingListView extends VerticalLayout {


    record Product(String name, double price) {
    }

    record Item(String name, double price, int qty) {
        Item withQty(int qty) {
            return new Item(name, price, qty);
        }
    }

    static final List<Product> PRODUCTS = List.of(
        new Product("Laptop", 999.99),
        new Product("Mouse", 25.99),
        new Product("Keyboard", 79.99),
        new Product("Monitor", 249.50));

    // Signals for state
    private final ListSignal<Item> cart = new ListSignal<>();
    private final Signal<Double> total = Signal.computed(() -> cart.get().stream()
        .mapToDouble(itemSignal -> {
            var item = itemSignal.get();
            return item.price() * item.qty();
        })
        .sum());

    public ShoppingListView() {

        add(new H3("Products"));
        // Products
        for (var product : PRODUCTS) {
            add(new HorizontalLayout(
                    new Span(product.name() + " — $" + product.price()),
                    new Button("Add", e -> cart.insertLast(
                        new Item(product.name(), product.price(), 1))
                    )
            ));
        }

        // Cart
        var cartList = new Div();
        cartList.bindChildren(cart, itemSignal -> {
            var quantity = new IntegerField();
            quantity.setStepButtonsVisible(true);
            quantity.setMin(1);
            quantity.setWidth("120px");
            quantity.bindValue(itemSignal.map(Item::qty), itemSignal.updater(Item::withQty));

            var itemTotal = new Span();
            itemTotal.bindText(itemSignal.map(item -> "$%.2f".formatted(item.price() * item.qty())));

            var row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            var nameLabel = new Span();
            nameLabel.bindText(itemSignal.map(Item::name));
            row.addAndExpand(nameLabel);
            row.add(quantity, itemTotal, new Button("X", e -> cart.remove(itemSignal)));
            return row;
        });

        var totalLabel = new H3();
        totalLabel.bindText(total.map("Total: $%.2f"::formatted));

        add(
            new HorizontalLayout(new H3("Cart"), new Button("Clear", e -> cart.clear())),
            cartList,
            totalLabel
        );
    }
}
