package com.example.application.ai;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.draiv.ai.presenter.TextPresenterService.Attachment;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnEvent;
import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnResult;
import com.vaadin.draiv.session.AssistPrincipal;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.streams.UploadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * The chat of the AI Driven UI area. Each message goes to draiv's {@link TextPresenterService}
 * together with the conversation so far; the model then operates the app in this very browser tab
 * through tool calls ({@code inspect}, {@code apply}, {@code navigate}), and changed fields glow.
 * <p>
 * Files dropped on the panel or picked with the paper clip belong to the conversation: images and PDFs
 * go to the model as attachments, text files (CSV, Markdown, plain text) as text, since Spring AI's
 * OpenAI client sends only images, audio and PDFs as media. They stay in the conversation until
 * "Neuer Chat".
 * <p>
 * The panel is draiv control chrome ({@code data-draiv-control}): the AI never sees or types into
 * the chat that drives it.
 */
public class AiChatPanel extends UploadDropZone {

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
            contact e-mail, net amount, VAT rate, category) next to a receipt upload; "Hinzufügen" adds it
            - ai/invoices: Rechnungen, all invoices: search, select one to edit it, "Neu", "Speichern"
            - ai/dashboard: AI Dashboard, spending charts
            - ai/macros: Makro, saved macros
            - ai/guardrails: Guardrails
            "Rechnung" means invoice, "Lieferant" supplier; "Rechnungs-Upload" or "Rechnung erfassen" \
            means the ai/form route. The chat panel on the right is not part of the app.
            Do what is asked and nothing more: fill in fields, but click a button such as \
            "Hinzufügen" only when the user asks for it. Answer in the user's language, in one or \
            two short sentences.
            The user may attach files (images, PDFs, CSV, Markdown, text) and ask about them; answer from \
            the file. If the user attaches an invoice and wants it entered, go to ai/form, fill in the form \
            from the file (invoice-number, invoice-supplier, invoice-contact-email, invoice-date and \
            invoice-due-date as ISO dates, invoice-net as a plain number, invoice-vat-rate as "0 %", "7 %" or "19 %", \
            invoice-category, invoice-receipt with the file name), then summarise supplier, number and \
            gross amount and ask "Soll ich die Rechnung so hinzufügen?". Click Hinzufügen \
            (invoice-add-button) only after a yes.""";

    private static final int MAX_FILES = 3;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB, phone photos are large
    /** Text files go into the message; this keeps one huge file from flooding the model. */
    private static final int MAX_TEXT_CHARS = 100_000;

    /** A file attached for the next message. */
    record Pending(String fileName, String mimeType, byte[] data) {

        /** Images and PDFs reach the model as media; everything else is read as text. */
        boolean isMedia() {
            return mimeType != null && (mimeType.startsWith("image/") || mimeType.equals("application/pdf"));
        }
    }

    private final TextPresenterService presenter;
    private final TextPresenterProperties presenterProperties;
    private final AssistPrincipal assistPrincipal;

    private final MessageList messages = new MessageList();
    private final MessageInput input = new MessageInput();
    private final VerticalLayout content = new VerticalLayout();
    private final UploadManager uploadManager = new UploadManager(this);
    private final Div attachments = new Div();
    private final List<Pending> pending = new ArrayList<>();
    private final Button newChat = new Button(VaadinIcon.PLUS_CIRCLE_O.create());
    private final Button hide = new Button(VaadinIcon.ANGLE_DOUBLE_RIGHT.create());
    /** The conversation the model sees, carried from turn to turn. */
    private List<Message> conversation = List.of(new UserMessage(APP_CONTEXT));

    public AiChatPanel(TextPresenterService presenter, TextPresenterProperties presenterProperties,
                       AssistPrincipal assistPrincipal) {
        this.presenter = presenter;
        this.presenterProperties = presenterProperties;
        this.assistPrincipal = assistPrincipal;
        getElement().getClassList().add("ai-chat-panel");
        getElement().setAttribute("data-draiv-control", "true");

        messages.setMarkdown(true);
        messages.addClassName("ai-chat-messages");
        greet();
        input.addSubmitListener(event -> send(event.getValue()));

        // Drop files anywhere on the panel, or pick them; on a phone the browser offers the camera
        uploadManager.setMaxFiles(MAX_FILES);
        uploadManager.setMaxFileSize(MAX_FILE_SIZE);
        uploadManager.setAcceptedMimeTypes("image/*", "application/pdf", "text/plain", "text/csv",
                "text/markdown");
        uploadManager.setAcceptedFileExtensions(".csv", ".md", ".txt"); // browsers often type these oddly
        uploadManager.setUploadHandler(UploadHandler.inMemory((metadata, data) ->
                received(metadata.fileName(), metadata.contentType(), data)));
        uploadManager.addAllFinishedListener(event -> uploadManager.clearFileList());
        uploadManager.addFileRejectedListener(event -> Notification.show(
                "Bilder, PDF, CSV, Markdown oder Text, bis zu 3 Dateien à 10 MB.")
                .addThemeVariants(NotificationVariant.WARNING));
        setUploadManager(uploadManager);

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.PAPERCLIP.create());
        uploadButton.setAriaLabel("Datei anhängen");
        uploadButton.setTooltipText("Datei anhängen (Bild, PDF, CSV, Markdown, Text)");
        uploadButton.setId("chat-attach-button");

        var uploadFileList = new UploadFileList(uploadManager); // progress while a file uploads
        uploadFileList.setWidthFull();
        attachments.addClassName("ai-chat-attachments");
        attachments.setId("chat-attachments");
        attachments.setVisible(false);

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

        content.addClassName("ai-chat-content");
        content.add(header, messages, uploadFileList, attachments, inputRow);
        content.setFlexGrow(1, messages);
        setContent(content);
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
        show(AI, "Hallo! Sag mir, was ich in der App tun soll, z. B. „Bring mich zum Rechnungs-Upload“. "
                + "Dateien kannst du hierher ziehen und dann Fragen dazu stellen.");
    }

    /** Forgets the conversation and the attached files: the model starts from the app context again. */
    private void startNewChat() {
        conversation = List.of(new UserMessage(APP_CONTEXT));
        pending.clear();
        showAttachments();
        messages.setItems(List.of());
        greet();
    }

    /** A message, with the files attached since the last one. */
    void send(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        var files = List.copyOf(pending);
        pending.clear();
        showAttachments();
        show(YOU, files.isEmpty() ? text : text + "\n\n" + files.stream().map(file -> "📎 " + file.fileName())
                .collect(Collectors.joining("  ")));
        var message = messageWithTextFiles(text, files);
        var media = files.stream().filter(Pending::isMedia)
                .map(file -> new Attachment(file.fileName(), file.mimeType(),
                        Base64.getEncoder().encodeToString(file.data())))
                .toList();
        startTurn(message, media);
    }

    /** The text files go into the message itself, each in a fenced block under its name. */
    static String messageWithTextFiles(String text, List<Pending> files) {
        var message = new StringBuilder(text);
        for (var file : files) {
            if (file.isMedia()) {
                message.append("\n\n(Attached: ").append(file.fileName()).append(")");
                continue;
            }
            var content = new String(file.data(), StandardCharsets.UTF_8);
            if (content.length() > MAX_TEXT_CHARS) {
                content = content.substring(0, MAX_TEXT_CHARS) + "\n[… cut off after " + MAX_TEXT_CHARS
                        + " characters]";
            }
            message.append("\n\nAttached file \"").append(file.fileName()).append("\":\n```\n")
                    .append(content).append("\n```");
        }
        return message.toString();
    }

    /** Upload callback, outside the UI lock: keep the file for the next message, call no model yet. */
    private void received(String fileName, String mimeType, byte[] data) {
        getUI().ifPresent(ui -> ui.access(() -> attach(fileName, mimeType, data)));
    }

    void attach(String fileName, String mimeType, byte[] data) {
        if (pending.size() >= MAX_FILES) {
            Notification.show("Höchstens " + MAX_FILES + " Dateien pro Nachricht.")
                    .addThemeVariants(NotificationVariant.WARNING);
            return;
        }
        pending.add(new Pending(fileName, typeOf(fileName, mimeType), data));
        showAttachments();
        show(AI, "Angehängt: " + fileName + ". Was möchtest du dazu wissen?");
    }

    /** Browsers send CSV and Markdown with all sorts of types; the extension decides then. */
    private static String typeOf(String fileName, String mimeType) {
        var name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".csv")) {
            return "text/csv";
        }
        if (name.endsWith(".md")) {
            return "text/markdown";
        }
        return mimeType == null || mimeType.isBlank() ? "text/plain" : mimeType;
    }

    /** The files waiting for the next message, as chips that can be removed. */
    private void showAttachments() {
        attachments.removeAll();
        for (var file : pending) {
            var remove = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> {
                pending.remove(file);
                showAttachments();
            });
            remove.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
            remove.setAriaLabel(file.fileName() + " entfernen");
            var chip = new Span(new Span("📎 " + file.fileName()), remove);
            chip.addClassName("ai-chat-attachment");
            attachments.add(chip);
        }
        attachments.setVisible(!pending.isEmpty());
    }

    /**
     * Runs one chat turn: {@code message} and the media attachments go to the model with the conversation
     * so far, and the reply replaces a placeholder.
     */
    private void startTurn(String message, List<Attachment> media) {
        if (!presenterProperties.isEnabled()) {
            show(AI, "AI ist aus: kein API-Key für " + presenterProperties.providerDisplayName() + ".");
            return;
        }
        var ui = UI.getCurrent();
        var principal = assistPrincipal.forCurrentUi(ui); // tells draiv which browser tab to drive
        var answer = show(AI, "…");
        var history = conversation;
        setBusy(true);

        // The model and tool loop takes seconds and drives this UI through ui.access(), so it must not
        // run on the request thread, which holds the session lock
        CompletableFuture.supplyAsync(() -> presenter.respond(principal, history, message, media,
                        event -> progress(ui, answer, event)))
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
