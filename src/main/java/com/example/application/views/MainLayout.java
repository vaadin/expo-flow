package com.example.application.views;


import com.example.application.components.appnav.AppNav;
import com.example.application.components.appnav.AppNavItem;
import com.example.application.views.crud.CRUDView;
import com.example.application.views.dashboard.DashboardView;
import com.example.application.views.map.MapView;
import com.example.application.views.sandbox.SandboxView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("My App");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        var image = new Image("images/vaadin.png", "Vaadin");
        image.setWidthFull();
        image.addClassNames("app-name");

        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, image, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();

        nav.addItem(new AppNavItem("CRUD", CRUDView.class, LineAwesomeIcon.COLUMNS_SOLID.create()));
        nav.addItem(new AppNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.CHART_AREA_SOLID.create()));
        nav.addItem(new AppNavItem("Map", MapView.class, LineAwesomeIcon.MAP_SOLID.create()));
        nav.addItem(new AppNavItem("Sandbox", SandboxView.class, LineAwesomeIcon.GLASSES_SOLID.create()));

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    /**
     * A simple navigation item component, based on ListItem element.
     */
//    public static class MenuItemInfo extends ListItem {
//
//        private final Class<? extends Component> view;
//
//        public MenuItemInfo(String menuTitle, LineAwesomeIcon icon, Class<? extends Component> view) {
//            this.view = view;
//            RouterLink link = new RouterLink();
//            link.addClassNames("menu-item-link");
//            link.setRoute(view);
//
//            Span text = new Span(menuTitle);
//            text.addClassNames("menu-item-text");
//
//            link.add(icon.create(), text);
//            add(link);
//        }
//
//        public Class<?> getView() {
//            return view;
//        }
//    }
//
//    private H1 viewTitle;
//
//    public MainLayout() {
//        setPrimarySection(Section.DRAWER);
//        addToNavbar(true, createHeaderContent());
//        addToDrawer(createDrawerContent());
//    }
//
//    private Component createHeaderContent() {
//        DrawerToggle toggle = new DrawerToggle();
//        toggle.addClassNames("view-toggle");
//        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
//        toggle.getElement().setAttribute("aria-label", "Menu toggle");
//
//        viewTitle = new H1();
//        viewTitle.addClassNames("view-title");
//
//        Header header = new Header(toggle, viewTitle);
//        header.addClassNames("view-header");
//        return header;
//    }
//
//    private Component createDrawerContent() {
//        var appName = new Image("images/vaadin.png", "Vaadin");
//        appName.addClassNames("app-name");
//
//        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName,
//                createNavigation(), createFooter());
//        section.addClassNames("drawer-section");
//        return section;
//    }
//
//    private Nav createNavigation() {
//        Nav nav = new Nav();
//        nav.addClassNames("menu-item-container");
//        nav.getElement().setAttribute("aria-labelledby", "views");
//
//        // Wrap the links in a list; improves accessibility
//        UnorderedList list = new UnorderedList();
//        list.addClassNames("navigation-list");
//        nav.add(list);
//
//        for (MenuItemInfo menuItem : createMenuItems()) {
//            list.add(menuItem);
//
//        }
//        return nav;
//    }
//
//    private MenuItemInfo[] createMenuItems() {
//        return new MenuItemInfo[]{ //
//                new MenuItemInfo("CRUD", LineAwesomeIcon.COLUMNS_SOLID, CRUDView.class), //
//
//                new MenuItemInfo("Dashboard", LineAwesomeIcon.CHART_AREA_SOLID, DashboardView.class), //
//
//                new MenuItemInfo("Map", LineAwesomeIcon.MAP_SOLID, MapView.class), //
//
//                new MenuItemInfo("Sandbox", LineAwesomeIcon.GLASSES_SOLID, SandboxView.class), //
//        };
//    }
//
//    private Footer createFooter() {
//        Footer layout = new Footer();
//        layout.addClassNames("footer");
//
//        return layout;
//    }
//
//    @Override
//    protected void afterNavigation() {
//        super.afterNavigation();
//        viewTitle.setText(getCurrentPageTitle());
//    }
//
//    private String getCurrentPageTitle() {
//        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
//        return title == null ? "" : title.value();
//    }
}
