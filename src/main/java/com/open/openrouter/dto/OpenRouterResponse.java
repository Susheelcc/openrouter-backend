package com.open.openrouter.dto;

import java.util.List;

import lombok.Data;

@Data
public class OpenRouterResponse {

    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message{
        private String role;
        private String content;
    }
}
