package com.example.application.ai;

import com.example.application.views.MainLayout;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.session.AssistPrincipal;
import com.vaadin.flow.automation.indication.ChangeIndication;
import com.vaadin.flow.automation.indication.ChangeIndication.IndicationStyle;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * The AI Driven UI area: every route under {@code ai/} renders here, inside {@link MainLayout}. A tab
 * bar of router links on top, the current tab on the left and the chat on the right. The chat
 * belongs to the layout, not to a tab, so the conversation survives the AI navigating between tabs.
 */
@Layout("ai")
@ParentLayout(MainLayout.class)
@AnonymousAllowed
@PageTitle("AI Driven UI")
public class AiShowcaseLayout extends Div implements RouterLayout {

    private final Div content = new Div();

    public AiShowcaseLayout(TextPresenterService presenter, TextPresenterProperties presenterProperties,
                            AssistPrincipal assistPrincipal) {
        addClassName("ai-showcase");
        var chatPanel = new AiChatPanel(presenter, presenterProperties, assistPrincipal);

        var tabs = new Nav(
                tab("1 · AI Grid", AiGridView.class),
                tab("2 · Form Filler", AiFormFillerView.class),
                tab("3 · Rechnung hochladen", AiInvoiceUploadView.class),
                tab("4 · AI Dashboard", AiDashboardView.class),
                tab("5 · Makro", AiMacroView.class),
                tab("6 · Guardrails", AiGuardrailsView.class));
        tabs.addClassName("ai-tabs");
        content.addClassName("ai-tab-content");

        add(tabs, content, chatPanel);
    }

    private static RouterLink tab(String text, Class<? extends Component> view) {
        var link = new RouterLink(text, view);
        link.setHighlightCondition(HighlightConditions.sameLocation());
        return link;
    }

    @Override
    public void showRouterLayoutContent(HasElement view) {
        content.removeAll();
        if (view != null) {
            content.getElement().appendChild(view.getElement());
        }
    }

    /** Every field the AI changes glows briefly, so visitors can follow what it does. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ChangeIndication.setStyle(attachEvent.getUI(), IndicationStyle.FLASH);
    }
}
