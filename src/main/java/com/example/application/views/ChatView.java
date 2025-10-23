package com.example.application.views;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Chat")
@Menu(title = "Chat", icon = LineAwesomeIconUrl.COMMENTS_SOLID, order = 4)
@Route(value = "chat")
public class ChatView extends VerticalLayout {

    private final MessageList messageList = new MessageList();
    private final TextField input = new TextField();
    private final Button sendButton = new Button("Send");
    private final List<MessageListItem> items = new ArrayList<>();
    HorizontalLayout form = new HorizontalLayout(input, sendButton);

    public ChatView(ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder.build();

        messageList.setItems(items);

        sendButton.addClickShortcut(Key.ENTER);
        sendButton.addClickListener(e -> {
            messageList.setMarkdown(true);
            String userText = input.getValue();
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
            input.clear();
        });

        setSizeFull();
        form.expand(input);
        form.setWidthFull();
        messageList.setSizeFull();
        add(messageList, form);
    }
}
