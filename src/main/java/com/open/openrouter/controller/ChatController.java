package com.open.openrouter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.openrouter.dto.ChatRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.open.openrouter.dto.ChatResponse;
import com.open.openrouter.service.ChatService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {


        return chatService.process(request);
    }

}
