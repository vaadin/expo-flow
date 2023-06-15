package com.example.application.service.openai;


import com.example.application.service.openai.data.ChatGPTResponse;
import com.example.application.service.openai.data.Message;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ChatGPTClient {

    @Value("${openai.api.key:no-key}")
    String openAiApiKey;
    private final WebClient webClient;

    public ChatGPTClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .build();
    }

    @RegisterReflectionForBinding(ChatGPTResponse.class)
    public Mono<String> generateCompletion(List<Message> messages) {

        if(openAiApiKey.equals("no-key"))
            return Mono.just("Please set the OPENAI_API_KEY environment variable to use this feature.");

        return webClient.post()
                .header("Authorization", "Bearer " + openAiApiKey)
                .bodyValue(Map.of(
                        "model", "gpt-3.5-turbo",
                        "messages", messages,
                        "max_tokens", 300
                ))
                .retrieve()
                .bodyToMono(ChatGPTResponse.class)
                .map(response -> response.getChoices().get(0).getMessage().getContent());
    }
}
