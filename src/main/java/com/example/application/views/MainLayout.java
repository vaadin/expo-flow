package com.example.application.views;


import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.Lumo;
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
        var image = new H2("Vaadin Flow");
        image.addClassNames("app-name");

        Header header = new Header(image);

        Scroller scroller = new Scroller(createNavigation());

        var themeToggle = new Checkbox("Dark theme");

        themeToggle.addValueChangeListener(e -> {
            var js = "document.documentElement.setAttribute('theme', $0)";
            getElement().executeJs(js, e.getValue() ? Lumo.DARK : Lumo.LIGHT);
        });

        addToDrawer(header, scroller, new VerticalLayout(themeToggle));
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Components", ComponentsView.class, LineAwesomeIcon.CUBES_SOLID.create()));
        nav.addItem(new SideNavItem("Layouts", LayoutsView.class, LineAwesomeIcon.COLUMNS_SOLID.create()));
        nav.addItem(new SideNavItem("Events", EventsView.class, LineAwesomeIcon.CALENDAR_ALT_SOLID.create()));
        nav.addItem(new SideNavItem("Form", FormView.class, LineAwesomeIcon.PEN_SOLID.create()));
        nav.addItem(new SideNavItem("Grid", GridView.class, LineAwesomeIcon.TABLE_SOLID.create()));
        nav.addItem(new SideNavItem("Push", PushView.class, LineAwesomeIcon.BELL_SOLID.create()));
        nav.addItem(new SideNavItem("Chat", ChatView.class, LineAwesomeIcon.COMMENTS_SOLID.create()));
        nav.addItem(new SideNavItem("Sandbox", SandboxView.class, LineAwesomeIcon.CODE_SOLID.create()));

        return nav;
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
}
