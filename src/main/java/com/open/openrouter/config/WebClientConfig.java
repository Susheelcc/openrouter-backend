package com.open.openrouter.config;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.open.openrouter.security.ApikeyFilter;

@Configuration
public class WebClientConfig {

    @Value("${openrouter.base-url}")
    private String baseUrl;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @PostConstruct
    public void debug() {
        System.out.println("Loaded API KEY from config: " + apiKey);
    }
    @Bean
    public WebClient webClient() {

            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("HTTP-Referer", "http://localhost:8090")
                    .defaultHeader("X-Title", "OpenRouter Backend App")
                    .build();
        }
    }
