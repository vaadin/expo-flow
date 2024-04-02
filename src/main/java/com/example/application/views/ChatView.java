package com.example.application.views;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.StreamingChatClient;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends VerticalLayout {

    public ChatView(StreamingChatClient chatClient) {
        setSizeFull();
        var messageList = new VerticalLayout();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        messageInput.addSubmitListener(event -> {
            var question = event.getValue();
            var userMessage = new MarkdownMessage(question, "You", MarkdownMessage.Color.AVATAR_PRESETS[0]);
            var assistantMessage = new MarkdownMessage("Assistant", MarkdownMessage.Color.AVATAR_PRESETS[1]);

            messageList.add(userMessage, assistantMessage);

            chatClient.stream(question).subscribe(assistantMessage::appendMarkdownAsync);
        });

        addAndExpand(new Scroller(messageList));
        add(messageInput);
    }
}
