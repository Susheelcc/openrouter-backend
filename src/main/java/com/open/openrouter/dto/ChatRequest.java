package com.open.openrouter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
@NotBlank
private String prompt;

@NotBlank
private String model;
}
