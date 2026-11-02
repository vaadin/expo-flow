package com.example.application.ai;

import com.example.application.ai.invoice.InvoiceDraft;
import com.example.application.ai.invoice.InvoiceExtractionService;
import com.example.application.ai.invoice.InvoiceForm;
import com.example.application.ai.invoice.InvoiceService;
import com.vaadin.draiv.ai.ui.AgentScope;
import com.vaadin.draiv.ai.ui.UiAssistant;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.UploadButton;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.UploadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Tab 2: drop a receipt (PDF or photo) next to the invoice form, and the AI fills in the form from it. The
 * file is read into an {@link InvoiceDraft} first ({@link InvoiceExtractionService}, off the UI thread);
 * then the AI enters the draft field by field through draiv, so the fields flash and the same validation
 * applies. It may only touch the form's fields ({@link AgentScope}): the status and "Hinzufügen" are out
 * of its reach, so a person checks the result and adds the invoice.
 */
@PageTitle("AI Form Filler")
@Route("ai/form")
public class AiFormFillerView extends AiTabView {

    private static final Logger log = LoggerFactory.getLogger(AiFormFillerView.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB, phone photos are large

    private final UiAssistant assistant;
    private final InvoiceExtractionService extraction;
    private final InvoiceForm form;
    private final AgentScope scope;

    private final Div problems = new Div();
    private final Div preview = new Div();
    private final ProgressBar progress = new ProgressBar();
    private final UploadManager uploadManager;

    public AiFormFillerView(UiAssistant assistant, InvoiceService invoiceService,
                            InvoiceExtractionService extraction) {
        super("AI Form Filler",
                "Beleg rechts ablegen: Die AI liest ihn und füllt das Formular aus, hinzufügen tut der Mensch.");
        this.assistant = assistant;
        this.extraction = extraction;
        this.form = new InvoiceForm(invoiceService);
        // The AI sees the form's fields, but neither the status nor the button that saves
        this.scope = AgentScope.within(form).excluding(form.getAddButton(), form.getStatusField());
        form.addInvoiceAddedListener(invoice -> clearReceipt());

        problems.addClassName("ai-draft-problems");
        problems.setId("draft-problems");
        problems.setVisible(false);
        var left = new Div(problems, form);
        left.addClassName("ai-form-filler-form");

        // The receipt: drop it anywhere on the right, or pick it; on a phone the browser offers the camera
        var dropZoneContent = new Div();
        uploadManager = new UploadManager(dropZoneContent);
        uploadManager.setMaxFiles(1);
        uploadManager.setMaxFileSize(MAX_FILE_SIZE);
        uploadManager.setAcceptedMimeTypes("image/*", "application/pdf");
        uploadManager.setUploadHandler(UploadHandler.inMemory((metadata, data) ->
                received(metadata.fileName(), metadata.contentType(), data)));
        uploadManager.addAllFinishedListener(event -> uploadManager.clearFileList());
        uploadManager.addFileRejectedListener(event -> Notification.show("Nur ein PDF oder Bild bis 10 MB.")
                .addThemeVariants(NotificationVariant.WARNING));

        var uploadButton = new UploadButton(uploadManager);
        uploadButton.setIcon(VaadinIcon.UPLOAD.create());
        uploadButton.setText("Beleg wählen");
        uploadButton.setId("receipt-upload-button");
        progress.setIndeterminate(true);
        progress.setVisible(false);
        preview.addClassName("ai-receipt-preview");
        preview.setId("receipt-preview");
        showPlaceholder();
        dropZoneContent.add(progress, preview, uploadButton);
        dropZoneContent.addClassName("ai-receipt-content");

        var dropZone = new UploadDropZone(dropZoneContent, uploadManager);
        dropZone.addClassName("ai-receipt");
        // People hand in the receipt; the AI only fills the form
        dropZone.getElement().setAttribute("data-draiv-control", "true");

        var columns = new Div(left, dropZone);
        columns.addClassName("ai-form-filler");
        add(columns);
    }

    /** Upload callback, outside the UI lock: show the receipt, then read it and let the AI fill the form. */
    private void received(String fileName, String mimeType, byte[] data) {
        getUI().ifPresent(ui -> ui.access(() -> {
            showPreview(fileName, mimeType, data);
            form.clear();
            showProblems(null);
            setBusy(true);
            CompletableFuture.supplyAsync(() -> extraction.extract(data, mimeType, fileName))
                    .whenComplete((draft, error) -> ui.access(() -> {
                        if (error != null) {
                            fail("Beleg konnte nicht gelesen werden", error);
                        } else {
                            fillForm(ui, draft, fileName);
                        }
                    }));
        }));
    }

    /** On the UI thread: the AI enters the draft, limited to the form. */
    void fillForm(UI ui, InvoiceDraft draft, String fileName) {
        showProblems(draft);
        assistant.assist(InvoiceDraftGoal.fillForm(draft, fileName), scope, null)
                .whenComplete((result, error) -> ui.access(() -> {
                    if (error != null) {
                        fail("Formular konnte nicht ausgefüllt werden", error);
                        return;
                    }
                    setBusy(false);
                    log.info("Form filler: {} tool call(s)", result.toolCalls().size());
                    Notification.show("Bitte prüfen und „Hinzufügen“ klicken.");
                }));
    }

    private void fail(String what, Throwable error) {
        setBusy(false);
        var cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        log.warn(what, cause);
        Notification.show(what + ": " + cause.getMessage()).addThemeVariants(NotificationVariant.ERROR);
    }

    /** What does not add up on the receipt, above the form; hidden when nothing is wrong. */
    void showProblems(InvoiceDraft draft) {
        problems.removeAll();
        var list = draft == null ? List.<String>of() : draft.problems();
        problems.setVisible(!list.isEmpty());
        if (!list.isEmpty()) {
            problems.add(new Span("Bitte prüfen:"),
                    new UnorderedList(list.stream().map(ListItem::new).toArray(ListItem[]::new)));
        }
    }

    void showPreview(String fileName, String mimeType, byte[] data) {
        var handler = DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(data), fileName, mimeType, data.length)).inline();
        preview.removeAll();
        if ("application/pdf".equals(mimeType)) {
            var pdf = new IFrame(handler);
            pdf.addClassName("ai-receipt-pdf");
            pdf.setTitle(fileName);
            preview.add(pdf);
        } else {
            preview.add(new Image(handler, fileName));
        }
    }

    private void showPlaceholder() {
        preview.removeAll();
        preview.add(new Span("PDF oder Foto einer Rechnung hierher ziehen"));
    }

    /** After "Hinzufügen": ready for the next receipt. */
    private void clearReceipt() {
        showPlaceholder();
        showProblems(null);
    }

    /** No second upload while one is read and entered. */
    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        uploadManager.setEnabled(!busy);
    }

    /** What the AI may touch while it fills in the form; for the tests. */
    AgentScope scope() {
        return scope;
    }
}
