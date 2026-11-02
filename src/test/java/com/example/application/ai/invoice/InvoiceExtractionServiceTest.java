package com.example.application.ai.invoice;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The model is faked: this checks what is sent and that its JSON answer becomes a draft. */
class InvoiceExtractionServiceTest {

    private Prompt sent;

    private final ChatModel model = prompt -> {
        sent = prompt;
        return new ChatResponse(List.of(new Generation(new AssistantMessage("""
                {"invoiceNumber": "RE-2026-114", "supplier": "Acme GmbH", "invoiceDate": "2026-10-28",
                 "dueDate": "2026-11-30", "net": null, "vatRatePercent": 19, "gross": 1190.00,
                 "category": "HARDWARE", "description": "Drei Monitore"}
                """))));
    };

    private final TextPresenterProperties properties = new TextPresenterProperties("openai", "", "gpt-5.5",
            "", "mistral-small-latest", "", "claude-haiku-4-5", 30, "minimal", "test-key", "", "");

    @Test
    void thePdfGoesToTheModelAndTheAnswerBecomesADraft() {
        var draft = new InvoiceExtractionService(model, properties)
                .extract(new byte[]{'%', 'P', 'D', 'F'}, "application/pdf", "acme.pdf");

        assertEquals("RE-2026-114", draft.invoiceNumber());
        assertEquals(LocalDate.of(2026, 11, 30), draft.dueDate());
        assertEquals(InvoiceCategory.HARDWARE, draft.category());
        assertEquals(new BigDecimal("1000.00"), draft.net()); // computed back from the gross
        assertEquals(List.of(), draft.problems());

        UserMessage user = (UserMessage) sent.getInstructions().getLast();
        // The JSON schema asks for the record's fields only, not its computed helpers
        assertTrue(user.getText().contains("\"vatRatePercent\""), user.getText());
        assertFalse(user.getText().contains("computedGross") || user.getText().contains("problems"), user.getText());
        assertEquals("application/pdf", user.getMedia().getFirst().getMimeType().toString());
        assertEquals("acme.pdf", user.getMedia().getFirst().getName());
        var options = (OpenAiChatOptions) sent.getOptions();
        assertEquals("gpt-5.5", options.getModel());
        assertEquals("none", options.getReasoningEffort()); // what draiv's table says gpt-5.5 needs
    }
}
