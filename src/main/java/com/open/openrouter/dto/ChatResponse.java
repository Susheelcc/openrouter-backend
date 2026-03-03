package com.open.openrouter.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {

    private String response;
    private String modelUsed;
    private LocalDateTime timestamp;
    private boolean fallback;
}
