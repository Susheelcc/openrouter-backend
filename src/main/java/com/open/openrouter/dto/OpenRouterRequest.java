package com.open.openrouter.dto;

import java.util.List;

import lombok.Data;


@Data
public class OpenRouterRequest {

    private String model;
    private Integer max_tokens;
    private List<Message> messages;

    @Data
    public static class Message{
        private String role;
        private String content;
        
    }


}
