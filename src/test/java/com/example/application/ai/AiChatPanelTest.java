package com.example.application.ai;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The chat panel's own controls, without a model. */
@SpringBootTest
class AiChatPanelTest extends SpringBrowserlessTest {

    @Test
    void hidingTheChatGivesTheTabTheWidthAndLastsForTheSession() {
        navigate(AiGridView.class);

        test(find(Button.class).id("chat-hide-button")).click();

        assertTrue(find(AiChatPanel.class).all().isEmpty()); // hidden
        assertTrue(find(AiShowcaseLayout.class).single().hasClassName("ai-chat-hidden"));

        navigate(AiDashboardView.class); // still hidden on another tab
        assertTrue(find(AiChatPanel.class).all().isEmpty());

        test(find(Button.class).id("chat-show-button")).click();
        assertFalse(find(AiChatPanel.class).all().isEmpty());
        assertFalse(find(AiShowcaseLayout.class).single().hasClassName("ai-chat-hidden"));
    }

    @Test
    void aNewChatForgetsTheConversation() {
        navigate(AiGridView.class);
        var panel = find(AiChatPanel.class).single();
        var messages = find(MessageList.class).single();
        messages.setItems(List.of(new MessageListItem("alt"), new MessageListItem("älter")));

        test(find(Button.class).id("chat-new-button")).click();

        assertEquals(1, messages.getItems().size()); // the greeting
        assertEquals(1, panel.conversation().size());
        assertTrue(panel.conversation().getFirst() instanceof UserMessage); // the app context only
    }
}
