package com.example.application.views;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Chat")
@Menu(title = "Chat", icon = LineAwesomeIconUrl.COMMENTS_SOLID, order = 4)
@Route(value = "chat")
public class ChatView extends VerticalLayout {

    private final MessageList messageList = new MessageList();
    private final MessageInput messageInput = new MessageInput();
    private final List<MessageListItem> items = new ArrayList<>();

    public ChatView(ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder.build();

        messageInput.addSubmitListener(e -> {
            String userText = e.getValue();
            if (userText == null || userText.isBlank()) {
                return;
            }
            MessageListItem userMsg = new MessageListItem(userText, Instant.now(), "You");
            userMsg.setUserColorIndex(4);
            items.add(userMsg);
            MessageListItem aiMsg = new MessageListItem("", Instant.now(), "Assistant");
            aiMsg.setUserColorIndex(1);
            items.add(aiMsg);
            UI ui = UI.getCurrent();
            chatClient.prompt().user(userText).stream().content()
                    .subscribe(t -> {
                        ui.access(() -> {
                            aiMsg.appendText(t);
                        });
                    });
            messageList.setItems(items);
        });

        setSizeFull();
        messageList.setMarkdown(true);
        messageList.setSizeFull();
        messageInput.setWidthFull();
        add(messageList, messageInput);
    }
}
