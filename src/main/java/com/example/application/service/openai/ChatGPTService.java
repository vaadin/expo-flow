package com.example.application.service.openai;

import com.example.application.service.openai.data.Message;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@UIScope
public class ChatGPTService {

    private final ChatGPTClient chatGPTClient;
    private final List<Message> messageHistory = new ArrayList<>();

    public ChatGPTService(ChatGPTClient chatGPTClient) {
        this.chatGPTClient = chatGPTClient;
        messageHistory.add(new Message("system", "You are a chat participant. You like to tell jokes and use puns."));
    }

    public Mono<String> getAnswer(String message) {
        messageHistory.add(new Message("user", message));
        return chatGPTClient.generateCompletion(messageHistory)
                .flatMap(answer -> {
                    messageHistory.add(new Message("system", answer));
                    return Mono.just(answer);
                });
    }
}
