package com.example.application.ai.invoice;

import com.vaadin.draiv.ai.presenter.OpenAiReasoning;
import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.util.List;

/**
 * Reads an uploaded invoice — a PDF or a photo — into an {@link InvoiceDraft} with Spring AI structured
 * output, with a certainty and the source text on the receipt for every value ({@link FieldEvidence}). Reading is separate from entering: draiv cannot operate an upload, so this hands the draft to
 * the chat, and the AI then enters it in the form on screen, where the person sees each field.
 * <p>
 * Uses the same model as the chat ({@code openai.text.model}). OpenAI only: Spring AI sends a PDF to
 * OpenAI as a file part, so no PDF rendering is needed here.
 */
@Service
public class InvoiceExtractionService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceExtractionService.class);

    private static final String INSTRUCTIONS = """
            Read the attached invoice (a PDF, scan or photo) and return its fields.
            - invoiceNumber: as printed.
            - supplier: the company that issued the invoice, not the recipient, with its legal form \
            (GmbH, AG, KG…).
            - contactEmail: the supplier's e-mail address for questions about the invoice, as printed; \
            null if there is none.
            - invoiceDate, dueDate: ISO dates. If only a payment term is printed ("zahlbar innerhalb \
            30 Tagen"), dueDate is that many days after invoiceDate.
            - net: the total net amount before VAT. gross: the total payable. Plain numbers, e.g. 1234.50.
            - vatRatePercent: the VAT rate in percent, e.g. 19. If there are several, the one on most \
            of the amount.
            - category: from what was bought. HARDWARE (devices, equipment), SOFTWARE (licences, \
            subscriptions, cloud), TRAVEL (hotel, train, flights, taxi), MARKETING (ads, print, \
            agency, trade shows), OFFICE (office supplies, furniture, everything else).
            - description: what was bought, in at most five German words.
            Use null for anything that is not on the invoice. Never guess a number.
            - evidence: one entry for each of invoiceNumber, supplier, contactEmail, invoiceDate, dueDate, net, \
            vatRatePercent, gross and category that has a value, with
              field: the field name as above;
              certainty: HIGH if printed plainly and read as is, MEDIUM if hard to read or derived from \
              what is printed (e.g. dueDate from a payment term), LOW if guessed or inferred (e.g. a \
              category judged from the items);
              source: the text on the invoice the value comes from, copied verbatim, at most 80 \
              characters, e.g. "Zahlbar bis 26.11.2026 ohne Abzug." (for a derived value, the text it is \
              derived from).""";

    private final ChatModel chatModel;
    private final TextPresenterProperties presenterProperties;

    public InvoiceExtractionService(ChatModel chatModel, TextPresenterProperties presenterProperties) {
        this.chatModel = chatModel;
        this.presenterProperties = presenterProperties;
    }

    /**
     * Blocks for a model call of a few seconds: call it off the UI thread.
     *
     * @param mimeType e.g. {@code application/pdf} or {@code image/jpeg}
     */
    public InvoiceDraft extract(byte[] data, String mimeType, String fileName) {
        if (!"openai".equals(presenterProperties.provider())) {
            throw new IllegalStateException("Reading invoices needs presenter.chat.provider=openai");
        }
        var model = presenterProperties.model();
        var options = OpenAiChatOptions.builder();
        options.model(model);
        // Reading a page needs no deliberation; this keeps the upload under ten seconds
        var effort = OpenAiReasoning.effortToSend(model, "minimal");
        if (effort != null) {
            options.reasoningEffort(effort);
        }
        var file = Media.builder()
                .mimeType(MimeType.valueOf(mimeType))
                .data(new ByteArrayResource(data) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                })
                .name(fileName)
                .build();

        // A Prompt with OpenAiChatOptions rather than ChatClient, so model and reasoning effort reach the
        // request as built, the same way draiv calls the model
        var converter = new BeanOutputConverter<>(InvoiceDraft.class);
        var prompt = new Prompt(UserMessage.builder()
                .text(INSTRUCTIONS + "\n\n" + converter.getFormat())
                .media(List.of(file))
                .build(), options.build());

        long start = System.nanoTime();
        var response = chatModel.call(prompt);
        var draft = converter.convert(response.getResult().getOutput().getText());
        log.info("Read invoice {} in {} ms: {}", fileName, (System.nanoTime() - start) / 1_000_000, draft);
        if (draft == null) {
            throw new IllegalStateException("The model returned no invoice data");
        }
        return draft.withNetFromGross();
    }
}
