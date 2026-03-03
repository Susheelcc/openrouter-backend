package com.open.openrouter.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.open.openrouter.client.OpenRouterClient;
import com.open.openrouter.dto.ChatRequest;
import com.open.openrouter.dto.ChatResponse;
import com.open.openrouter.dto.OpenRouterRequest;
import com.open.openrouter.dto.OpenRouterResponse;




@Service
public class ChatService {

    private final OpenRouterClient openRouterClient;

    @Value("${openrouter.default-model}")
    private String defaultModel;

    public ChatService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public ChatResponse process(ChatRequest request) {

        String model =
                request.getModel() != null
                        ? request.getModel()
                        : defaultModel;

        OpenRouterRequest orRequest = new OpenRouterRequest();
        orRequest.setModel(model);

        OpenRouterRequest.Message msg = new OpenRouterRequest.Message();
        msg.setRole("user");
        msg.setContent(request.getPrompt());

        orRequest.setMessages(List.of(msg));

        OpenRouterResponse orResponse =
                openRouterClient.call(orRequest);

        // fallback
        if (orResponse == null || orResponse.getChoices() == null) {
            return ChatResponse.builder()
                    .response("AI service temporarily unavailable")
                    .modelUsed(model)
                    .timestamp(LocalDateTime.now())
                    .fallback(true)
                    .build();
        }

        String aiText =
                orResponse.getChoices()
                        .get(0)
                        .getMessage()
                        .getContent();

        return ChatResponse.builder()
                .response(aiText)
                .modelUsed(model)
                .timestamp(LocalDateTime.now())
                .fallback(false)
                .build();
    }
}