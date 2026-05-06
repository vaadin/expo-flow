package com.example.application.views;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

@Layout
@AnonymousAllowed
@JavaScript("./color-cycle.js")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;
    private boolean darkMode = false;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1(
                new Span("Vaadin"){{addClassNames("logo-text");}},
                new Span("}>"){{addClassNames("logo-symbol");}}
        );
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            if (entry.icon() != null) {
                nav.addItem(new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon())));
            } else {
                nav.addItem(new SideNavItem(entry.title(), entry.path()));
            }
        });

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        var darkModeBtn = new Button(VaadinIcon.MOON.create());
        darkModeBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        darkModeBtn.setTooltipText("Dark mode");
        darkModeBtn.addClickListener(this::onToggleDarkMode);

        var themeSelect = new ComboBox<String>("Theme");
        themeSelect.setItems("Default", "Carbon", "Sparkasse", "DHL", "Deutsche Bank", "Less Carbon", "Linear");
        themeSelect.setAllowCustomValue(false);
        themeSelect.addValueChangeListener(this::onThemeSelect);
        themeSelect.setValue("Default");

        var row = new Div(darkModeBtn, new Checkbox("Unicorn mode", this::changeUnicornMode));
        var controls = new Div(themeSelect, row);

        layout.add(controls);
        return layout;
    }

    private void onToggleDarkMode(ClickEvent<Button> event) {

        event.getSource().getUI().ifPresent(ui -> {
            var button = event.getSource();

            darkMode = !darkMode;

            if (darkMode) {
                ui.getPage().executeJs("document.documentElement.style.colorScheme = 'dark'");
                button.setIcon(VaadinIcon.SUN_O.create());
                button.setTooltipText("Light mode");
            } else {
                ui.getCurrent().getPage().executeJs("document.documentElement.style.colorScheme = 'light'");
                button.setIcon(VaadinIcon.MOON.create());
                button.setTooltipText("Dark mode");
            }
        });

    }

    private void onThemeSelect(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> event) {
        event.getSource().getUI().ifPresent(ui -> {
            var theme = event.getValue();
            Registration previous = ComponentUtil.getData(ui, Registration.class);
            if (previous != null) {
                previous.remove();
            }
            String stylesheet = "themes/" + theme.toLowerCase().replace(" ", "-") + "-theme.css";
            Registration registration = ui.getPage().addStyleSheet(stylesheet);
            ComponentUtil.setData(ui, Registration.class, registration);
        });

    }


    private void changeUnicornMode(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        event.getSource().getUI().ifPresent(ui -> {
            if (event.getValue()) {
                ui.getPage().executeJs("window.colorCycle.start()");
            } else {
                ui.getPage().executeJs("window.colorCycle.stop()");
            }
        });
    }


    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
    }
}
