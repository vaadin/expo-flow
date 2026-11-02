package com.example.application.ai;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnEvent;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnResult;
import com.vaadin.draiv.session.AssistPrincipal;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.streams.UploadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * The chat of the AI Driven UI area. Each message goes to draiv's {@link TextPresenterService}
 * together with the conversation so far; the model then operates the app in this very browser tab
 * through tool calls ({@code inspect}, {@code apply}, {@code navigate}), and changed fields glow.
 * <p>
 * The panel is draiv control chrome ({@code data-draiv-control}): the AI never sees or types into
 * the chat that drives it.
 */
public class AiChatPanel extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AiChatPanel.class);
    private static final String AI = "AI";
    private static final String YOU = "Du";

    /**
     * What the model needs to know about this app, sent as the first message of every conversation.
     * draiv's own system prompt covers how to use the tools.
     */
    private static final String APP_CONTEXT = """
            You operate the "AI Driven UI" area of the Vaadin app Expo Flow for a trade-show visitor, \
            in this very browser tab. The area has these routes (pass them to navigate as they are):
            - ai/grid: AI Grid, find invoices by filtering a grid
            - ai/form: Form Filler, turn a supplier e-mail into an invoice form
            - ai/upload: Rechnung hochladen, the invoice form for entering an invoice (supplier, \
            number, dates, net amount, VAT rate, category, status) and the latest invoices
            - ai/dashboard: AI Dashboard, spending charts
            - ai/macros: Makro, saved macros
            - ai/guardrails: Guardrails
            "Rechnung" means invoice, "Lieferant" supplier, "Rechnungs-Upload" or "Rechnung erfassen" \
            the ai/upload route. The chat panel on the right is not part of the app.
            Do what is asked and nothing more: fill in fields, but click a button such as \
            "Hinzufügen" only when the user asks for it. Answer in the user's language, in one or \
            two short sentences.""";

    private final TextPresenterService presenter;
    private final TextPresenterProperties presenterProperties;
    private final AssistPrincipal assistPrincipal;

    private final MessageList messages = new MessageList();
    private final MessageInput input = new MessageInput();
    private final UploadManager uploadManager = new UploadManager(this);
    /** The conversation the model sees, carried from turn to turn. */
    private List<Message> conversation = List.of(new UserMessage(APP_CONTEXT));

    public AiChatPanel(TextPresenterService presenter, TextPresenterProperties presenterProperties,
                       AssistPrincipal assistPrincipal) {
        this.presenter = presenter;
        this.presenterProperties = presenterProperties;
        this.assistPrincipal = assistPrincipal;
        addClassName("ai-chat-panel");
        getElement().setAttribute("data-draiv-control", "true");

        messages.setMarkdown(true);
        messages.addClassName("ai-chat-messages");
        show(AI, "Hallo! Sag mir, was ich in der App tun soll, z. B. „Bring mich zum Rechnungs-Upload“.");
        input.addSubmitListener(event -> send(event.getValue()));

        // Invoices as PDF or photo; on a phone the browser offers the camera
        uploadManager.setMaxFiles(5);
        uploadManager.setMaxFileSize(10 * 1024 * 1024); // 10 MB, phone photos are large
        uploadManager.setAcceptedMimeTypes("image/*", "application/pdf");
        uploadManager.setUploadHandler(UploadHandler.inMemory((metadata, data) ->
                received(metadata.fileName(), data.length)));
        uploadManager.addAllFinishedListener(event -> uploadManager.clearFileList());

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.PAPERCLIP.create());
        uploadButton.setAriaLabel("Rechnung anhängen");
        uploadButton.setTooltipText("Rechnung anhängen (PDF oder Foto)");

        var uploadFileList = new UploadFileList(uploadManager);
        uploadFileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);
        uploadFileList.setWidthFull();

        var inputRow = new HorizontalLayout(uploadButton, input);
        inputRow.setWidthFull();
        inputRow.setFlexGrow(1, input);
        inputRow.setAlignItems(FlexComponent.Alignment.BASELINE);
        inputRow.setSpacing(false);

        add(new H3("AI-Assistent"), messages, uploadFileList, inputRow);
        setFlexGrow(1, messages);
    }

    private void send(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        show(YOU, text);
        if (!presenterProperties.isEnabled()) {
            show(AI, "AI ist aus: kein API-Key für " + presenterProperties.providerDisplayName() + ".");
            return;
        }
        var ui = UI.getCurrent();
        var principal = assistPrincipal.forCurrentUi(ui); // tells draiv which browser tab to drive
        var answer = show(AI, "…");
        var history = conversation;
        input.setEnabled(false);

        // The model and tool loop takes seconds and drives this UI through ui.access(), so it must not
        // run on the request thread, which holds the session lock
        CompletableFuture.supplyAsync(() -> presenter.respond(principal, history, text, List.of(),
                        event -> progress(ui, answer, event)))
                .whenComplete((turn, error) -> ui.access(() -> {
                    input.setEnabled(true);
                    if (error != null) {
                        log.warn("AI chat failed", error);
                        answer.setText("Das hat nicht geklappt: " + rootMessage(error));
                    } else {
                        conversation = turn.conversationItems();
                        answer.setText(answerText(turn));
                    }
                }));
    }

    /** While the AI works, its message lists the tools it calls, so visitors see something happen. */
    private void progress(UI ui, MessageListItem answer, TurnEvent event) {
        if (!"tool_call".equals(event.type())) {
            return;
        }
        log.info("AI chat tool call: {} {}", event.data().get("name"), event.data().get("args"));
        ui.access(() -> answer.appendText(" `" + event.data().get("name") + "`"));
    }

    private static String answerText(TurnResult turn) {
        if (turn.text() != null && !turn.text().isBlank()) {
            return turn.text();
        }
        return turn.toolCalls().isEmpty() ? "Erledigt." : "Erledigt (" + turn.toolCalls().size() + " Schritte).";
    }

    /**
     * A file arrived. Reading the invoice from it is the next step; for now the chat confirms it.
     */
    private void received(String fileName, int size) {
        getUI().ifPresent(ui -> ui.access(() -> show(AI, "Beleg „%s“ (%d KB) ist angekommen. Auslesen kann ich ihn noch nicht."
                .formatted(fileName, Math.max(1, size / 1024)))));
    }

    private MessageListItem show(String user, String text) {
        var item = new MessageListItem(text, Instant.now(), user);
        item.setUserColorIndex(AI.equals(user) ? 1 : 2);
        var items = new ArrayList<>(messages.getItems());
        items.add(item);
        messages.setItems(items);
        return item;
    }

    /** draiv words provider errors (no credit, rate limit) for people; unwrap down to that. */
    private static String rootMessage(Throwable error) {
        var cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
