package com.example.application.views;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.Instant;

@PageTitle("Chat")
@Menu(title = "Chat", icon = LineAwesomeIconUrl.COMMENTS_SOLID, order = 4)
@Route(value = "chat")
public class ChatView extends VerticalLayout {

    public ChatView(ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder.build();
        setSizeFull();
        var messageList = new MessageList();
        var messageInput = new MessageInput();
        messageList.setMarkdown(true);
        messageInput.setWidthFull();

        messageInput.addSubmitListener(event -> {
            var question = event.getValue();
            var userMessage = new MessageListItem(question, Instant.now(),"You");
            userMessage.setUserColorIndex(1);
            messageList.addItem(userMessage);

            var assistantMessage = new MessageListItem("Assistant");
            assistantMessage.setUserColorIndex(2);
            messageList.addItem(assistantMessage);

            chatClient.prompt()
                    .user(question)
                    .stream()
                    .content()
                    .subscribe(token ->
                            event.getUI().access(() ->
                                    assistantMessage.appendText(token))
                    );
        });

        addAndExpand(new Scroller(messageList));
        add(messageInput);
    }
}
