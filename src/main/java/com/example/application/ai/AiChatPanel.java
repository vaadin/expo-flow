package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceDraft;
import com.example.application.ai.invoice.InvoiceExtractionService;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnEvent;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnResult;
import com.vaadin.draiv.session.AssistPrincipal;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

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
            - ai/form: AI Form Filler, the form for entering a new invoice (supplier, number, dates, \
            net amount, VAT rate, category) next to a receipt upload; "Hinzufügen" adds it
            - ai/invoices: Rechnungen, all invoices: search, select one to edit it, "Neu", "Speichern"
            - ai/dashboard: AI Dashboard, spending charts
            - ai/macros: Makro, saved macros
            - ai/guardrails: Guardrails
            "Rechnung" means invoice, "Lieferant" supplier; "Rechnungs-Upload" or "Rechnung erfassen" \
            means the ai/form route. The chat panel on the right is not part of the app.
            Do what is asked and nothing more: fill in fields, but click a button such as \
            "Hinzufügen" only when the user asks for it. Answer in the user's language, in one or \
            two short sentences.""";

    private static final NumberFormat EURO = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private final TextPresenterService presenter;
    private final TextPresenterProperties presenterProperties;
    private final AssistPrincipal assistPrincipal;
    private final InvoiceExtractionService extraction;

    private final MessageList messages = new MessageList();
    private final MessageInput input = new MessageInput();
    private final UploadManager uploadManager = new UploadManager(this);
    private final Button newChat = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
    private final Button hide = new Button(VaadinIcon.ANGLE_DOUBLE_RIGHT.create());
    /** The conversation the model sees, carried from turn to turn. */
    private List<Message> conversation = List.of(new UserMessage(APP_CONTEXT));

    public AiChatPanel(TextPresenterService presenter, TextPresenterProperties presenterProperties,
                       AssistPrincipal assistPrincipal, InvoiceExtractionService extraction) {
        this.presenter = presenter;
        this.presenterProperties = presenterProperties;
        this.assistPrincipal = assistPrincipal;
        this.extraction = extraction;
        addClassName("ai-chat-panel");
        getElement().setAttribute("data-draiv-control", "true");

        messages.setMarkdown(true);
        messages.addClassName("ai-chat-messages");
        greet();
        input.addSubmitListener(event -> send(event.getValue()));

        // Invoices as PDF or photo; on a phone the browser offers the camera
        uploadManager.setMaxFiles(1); // one invoice at a time
        uploadManager.setMaxFileSize(10 * 1024 * 1024); // 10 MB, phone photos are large
        uploadManager.setAcceptedMimeTypes("image/*", "application/pdf");
        uploadManager.setUploadHandler(UploadHandler.inMemory((metadata, data) ->
                received(metadata.fileName(), metadata.contentType(), data)));
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

        newChat.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        newChat.setAriaLabel("Neuer Chat");
        newChat.setTooltipText("Neuer Chat: Verlauf und Anhänge vergessen");
        newChat.setId("chat-new-button");
        newChat.addClickListener(e -> startNewChat());
        hide.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        hide.setAriaLabel("Chat ausblenden");
        hide.setTooltipText("Chat ausblenden");
        hide.setId("chat-hide-button");
        var header = new HorizontalLayout(new H3("AI-Assistent"), newChat, hide);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, header.getComponentAt(0));
        header.addClassName("ai-chat-header");

        add(header, messages, uploadFileList, inputRow);
        setFlexGrow(1, messages);
    }

    /** The conversation the model sees next; for the tests. */
    List<Message> conversation() {
        return conversation;
    }

    /** The button that hides the panel; the layout around it decides what hiding means. */
    public Button getHideButton() {
        return hide;
    }

    private void greet() {
        show(AI, "Hallo! Sag mir, was ich in der App tun soll, z. B. „Bring mich zum Rechnungs-Upload“, "
                + "oder häng mit der Büroklammer eine Rechnung an.");
    }

    /** Forgets the conversation: the model starts from the app context again. */
    private void startNewChat() {
        conversation = List.of(new UserMessage(APP_CONTEXT));
        messages.setItems(List.of());
        greet();
    }

    private void send(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        show(YOU, text);
        startTurn("…", answer -> text);
    }

    /**
     * An invoice arrived (upload callback, outside the UI lock). It is read into a draft first; then the
     * AI enters the draft in the form on screen and asks before adding it.
     */
    private void received(String fileName, String mimeType, byte[] data) {
        getUI().ifPresent(ui -> ui.access(() -> {
            show(YOU, "📎 " + fileName);
            startTurn("Lese die Rechnung …", answer -> {
                var draft = extraction.extract(data, mimeType, fileName);
                ui.access(() -> answer.setText("Rechnung gelesen, trage sie ein …"));
                return enterDraftRequest(draft, fileName);
            });
        }));
    }

    /**
     * Runs one chat turn: {@code message} (computed off the UI thread, it may block) goes to the model
     * with the conversation so far, and the reply replaces {@code placeholder}.
     */
    private void startTurn(String placeholder, Function<MessageListItem, String> message) {
        if (!presenterProperties.isEnabled()) {
            show(AI, "AI ist aus: kein API-Key für " + presenterProperties.providerDisplayName() + ".");
            return;
        }
        var ui = UI.getCurrent();
        var principal = assistPrincipal.forCurrentUi(ui); // tells draiv which browser tab to drive
        var answer = show(AI, placeholder);
        var history = conversation;
        setBusy(true);

        // The model and tool loop takes seconds and drives this UI through ui.access(), so it must not
        // run on the request thread, which holds the session lock
        CompletableFuture.supplyAsync(() -> presenter.respond(principal, history, message.apply(answer),
                        List.of(), event -> progress(ui, answer, event)))
                .whenComplete((turn, error) -> ui.access(() -> {
                    setBusy(false);
                    if (error != null) {
                        log.warn("AI chat failed", error);
                        answer.setText("Das hat nicht geklappt: " + rootMessage(error));
                    } else {
                        conversation = turn.conversationItems();
                        answer.setText(answerText(turn));
                    }
                }));
    }

    /** One turn at a time: a second message or upload would drive the same UI concurrently. */
    private void setBusy(boolean busy) {
        input.setEnabled(!busy);
        newChat.setEnabled(!busy);
        uploadManager.setEnabled(!busy);
    }

    /**
     * What the model is told after an upload: the values read, what does not add up, and how to enter
     * them. Adding waits for the user's yes, which comes as an ordinary chat message.
     */
    static String enterDraftRequest(InvoiceDraft draft, String fileName) {
        var problems = draft.problems();
        return """
                I uploaded the invoice "%s". It was read as:
                - Rechnungsnummer (invoice-number): %s
                - Lieferant (invoice-supplier): %s
                - Rechnungsdatum (invoice-date): %s
                - Fällig am (invoice-due-date): %s
                - Netto (invoice-net): %s
                - MwSt-Satz (invoice-vat-rate): %s
                - Kategorie (invoice-category): %s
                - Beleg (invoice-receipt): %s
                Gross amount: %s. What was bought: %s.
                Checks: %s

                Go to ai/form unless it is already shown. Inspect once, then in a single apply_many                 click Zurücksetzen (invoice-reset-button) and set every field that has a value, with                 capability "settable", operation "set"; leave "none" fields empty. Do NOT click                 Hinzufügen.
                Then reply in German in two short sentences: supplier, invoice number and gross amount;                 any check problem; and ask "Soll ich die Rechnung so hinzufügen?". If I correct                 something, change that field and ask again. Only after I say yes, click Hinzufügen                 (invoice-add-button).
                """.formatted(fileName,
                orNone(draft.invoiceNumber()), orNone(draft.supplier()),
                orNone(draft.invoiceDate()), orNone(draft.dueDate()),
                draft.net() == null ? "none" : draft.net().toPlainString(),
                draft.vatRate() == null ? "none" : "\"" + draft.vatRate().label() + "\"",
                draft.category() == null ? "none" : "\"" + draft.category().label() + "\"",
                fileName,
                draft.computedGross() == null ? "unknown" : EURO.format(draft.computedGross()),
                orNone(draft.description()),
                problems.isEmpty() ? "all fine" : String.join(" ", problems));
    }

    private static String orNone(Object value) {
        return value == null || value.toString().isBlank() ? "none" : value.toString();
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
