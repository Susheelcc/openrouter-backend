package com.open.openrouter.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openrouter.base-url}")
    private String baseUrl;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Bean
    public WebClient webClient() {
            if (!StringUtils.hasText(apiKey)) {
                throw new IllegalStateException("OPENROUTER_API_KEY is missing");
            }

            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("HTTP-Referer", "http://localhost:8090")
                    .defaultHeader("X-Title", "OpenRouter Backend App")
                    .build();
        }
    }
