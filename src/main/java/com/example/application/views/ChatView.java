package com.example.application.views;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;
import org.vaadin.lineawesome.LineAwesomeIcon;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Chat")
@Menu(title = "Chat", icon = LineAwesomeIconUrl.COMMENTS_SOLID, order = 4)
@Route(value = "chat")
public class ChatView extends VerticalLayout {

    public ChatView(ChatClient.Builder chatClientBuilder) {
        ChatClient chatClient = chatClientBuilder.build();
        setSizeFull();
        var messageList = new VerticalLayout();
        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        messageInput.addSubmitListener(event -> {
            var question = event.getValue();
            var userMessage = new MarkdownMessage(question, "You", MarkdownMessage.Color.AVATAR_PRESETS[0]);
            var assistantMessage = new MarkdownMessage("Assistant", MarkdownMessage.Color.AVATAR_PRESETS[1]);

            messageList.add(userMessage, assistantMessage);

            chatClient.prompt().user(question).stream().content()
                    .subscribe(assistantMessage::appendMarkdownAsync);
        });

        addAndExpand(new Scroller(messageList));
        add(messageInput);
    }
}
