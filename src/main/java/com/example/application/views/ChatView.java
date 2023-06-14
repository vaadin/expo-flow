package com.example.application.views;

import com.example.application.service.openai.ChatGPTService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Instant;
import java.util.ArrayList;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
public class ChatView extends VerticalLayout {

    private final MessageList messageList;
    private final MessageInput messageInput;

    public ChatView(ChatGPTService chatGPT) {
        setSizeFull();
        messageList = new MessageList();
        messageInput = new MessageInput();
        messageInput.setWidthFull();

        messageInput.addSubmitListener(event -> {
            var message = event.getValue();
            addMessage(new MessageListItem(message, Instant.now(), "You"));

            var ui = UI.getCurrent();
            chatGPT.getAnswer(message).subscribe(answer -> {
                ui.access(() ->
                        addMessage(new MessageListItem(answer, Instant.now(), "Bot")));
            });
        });

        addAndExpand(messageList);
        add(messageInput);
    }

    private void addMessage(MessageListItem message) {
        var items = new ArrayList<>(messageList.getItems());
        items.add(message);
        messageList.setItems(items);
    }
}
