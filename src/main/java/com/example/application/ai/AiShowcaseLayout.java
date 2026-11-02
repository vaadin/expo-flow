package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceService;
import com.example.application.views.MainLayout;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.session.AssistPrincipal;
import com.vaadin.flow.automation.indication.ChangeIndication;
import com.vaadin.flow.automation.indication.ChangeIndication.IndicationStyle;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
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

    private static final String CHAT_HIDDEN = AiShowcaseLayout.class.getName() + ".chatHidden";

    private final Div content = new Div();
    private final AiChatPanel chatPanel;
    private final Button showChat;

    public AiShowcaseLayout(TextPresenterService presenter, TextPresenterProperties presenterProperties,
                            AssistPrincipal assistPrincipal,
                            InvoiceService invoiceService, ScriptStore scripts) {
        addClassName("ai-showcase");
        var chatPanel = new AiChatPanel(presenter, presenterProperties, assistPrincipal);

        var tabs = new Nav(
                tab("1 · AI Grid", AiGridView.class),
                tab("2 · AI Form Filler", AiFormFillerView.class),
                tab("3 · Rechnungen", AiInvoiceCrudView.class),
                tab("4 · AI Dashboard", AiDashboardView.class),
                tab("5 · Makro", AiMacroView.class),
                tab("6 · Guardrails", AiGuardrailsView.class));
        tabs.addClassName("ai-tabs");
        content.addClassName("ai-tab-content");

        // Hiding the chat gives the tab the full width; the conversation stays, and so does the choice
        // for the rest of the session
        var showChat = new Button("Chat", VaadinIcon.COMMENTS_O.create(), e -> setChatHidden(true, false));
        showChat.addThemeVariants(ButtonVariant.TERTIARY);
        showChat.setTooltipText("Chat einblenden");
        showChat.setId("chat-show-button");
        showChat.getElement().setAttribute("data-draiv-control", "true");
        this.showChat = showChat;
        this.chatPanel = chatPanel;
        chatPanel.getHideButton().addClickListener(e -> setChatHidden(true, true));

        var tabBar = new Div(tabs, new Div(showChat, resetButton(invoiceService, scripts)));
        tabBar.addClassName("ai-tab-bar");

        add(tabBar, content, chatPanel);
        setChatHidden(false, Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute(CHAT_HIDDEN)));
    }

    /** Shows or hides the chat; {@code remember} keeps the choice in the session. */
    private void setChatHidden(boolean remember, boolean hidden) {
        chatPanel.setVisible(!hidden);
        showChat.setVisible(hidden);
        setClassName("ai-chat-hidden", hidden);
        if (remember) {
            VaadinSession.getCurrent().setAttribute(CHAT_HIDDEN, hidden);
        }
    }

    /**
     * "Demo zurücksetzen", between two visitors: the demo invoices, no macros, and a reload, which also
     * starts a fresh chat.
     */
    private static Button resetButton(InvoiceService invoiceService, ScriptStore scripts) {
        var reset = new Button("Demo zurücksetzen", VaadinIcon.REFRESH.create(), e -> {
            var confirm = new ConfirmDialog("Demo zurücksetzen?",
                    "Rechnungen und Makros gehen auf den Ausgangsstand zurück, der Chat beginnt neu.",
                    "Zurücksetzen", confirmed -> {
                        invoiceService.resetAndSeed();
                        scripts.list().forEach(scripts::delete);
                        UI.getCurrent().getPage().reload();
                    });
            confirm.setCancelable(true);
            confirm.setCancelText("Abbrechen");
            confirm.open();
        });
        reset.addThemeVariants(ButtonVariant.TERTIARY);
        reset.setId("demo-reset-button");
        // Booth staff only: the AI never resets the demo
        reset.getElement().setAttribute("data-draiv-control", "true");
        return reset;
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
