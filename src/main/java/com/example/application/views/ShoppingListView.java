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
public class ShoppingListView extends HorizontalLayout {

    record Product(String name, double price) {}

    record Item(String name, double price, int qty) {
        Item withQty(int qty) { return new Item(name, price, qty); }
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
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // --- Left: Products ---
        var products = new VerticalLayout();
        products.setSpacing(true);
        products.setPadding(false);
        products.setWidth(null);

        products.add(new H3("Products"));
        for (var product : PRODUCTS) {
            var row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.setSpacing(true);
            var label = new Span(product.name() + " — $" + product.price());
            label.setWidth("200px");
            row.add(label, new Button("Add", e -> cart.insertLast(
                    new Item(product.name(), product.price(), 1))));
            products.add(row);
        }

        // --- Right: Cart ---
        var cartPanel = new VerticalLayout();
        cartPanel.addClassName("aura-surface");
        cartPanel.addClassName("shopping-cart-panel");
        cartPanel.setSpacing(true);

        var cartHeader = new HorizontalLayout(new H3("Cart"), new Button("Clear", e -> cart.clear()));
        cartHeader.setAlignItems(Alignment.CENTER);

        var cartList = new Div();
        cartList.bindChildren(cart, itemSignal -> {
            var quantity = new IntegerField();
            quantity.setStepButtonsVisible(true);
            quantity.setMin(1);
            quantity.setWidth("120px");
            quantity.bindValue(itemSignal.map(Item::qty), itemSignal.updater(Item::withQty));

            var itemTotal = new Span();
            itemTotal.bindText(itemSignal.map(item -> "$%.2f".formatted(item.price() * item.qty())));
            itemTotal.setWidth("100px");

            var row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(Alignment.CENTER);
            var nameLabel = new Span();
            nameLabel.bindText(itemSignal.map(Item::name));
            row.addAndExpand(nameLabel);
            row.add(quantity, itemTotal, new Button("X", e -> cart.remove(itemSignal)));
            return row;
        });

        var totalLabel = new H3();
        totalLabel.bindText(total.map("Total: $%.2f"::formatted));

        cartPanel.add(cartHeader, cartList, totalLabel);

        addAndExpand(cartPanel);
        addComponentAsFirst(products);
    }
}
