package com.open.openrouter.client;

import com.open.openrouter.dto.OpenRouterRequest;
import com.open.openrouter.dto.OpenRouterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component
public class OpenRouterClient {

    private static final Logger log =
            LoggerFactory.getLogger(OpenRouterClient.class);

    private final WebClient webClient;

    @Value("${openrouter.timeout-seconds}")
    private int timeoutSeconds;

    public OpenRouterClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public OpenRouterResponse call(OpenRouterRequest request) {

        log.info("Calling OpenRouter | model={}", request.getModel());

        try {
            return webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenRouterResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

        } catch (WebClientResponseException e) {

            // ---- HTTP errors from OpenRouter ----
            if (e.getStatusCode().value() == 402) {
                log.error("OpenRouter 402 Payment Required");
                throw new RuntimeException(
                        "AI service unavailable: insufficient credits or invalid API key"
                );
            }

            log.error("OpenRouter HTTP error: {}", e.getStatusCode());
            throw new RuntimeException(
                    "AI service error: " + e.getStatusCode()
            );

        } catch (Exception e) {

            // ---- Timeout / network / unknown errors ----
            log.error("OpenRouter call failed", e);
            return null; // fallback handled in service
        }
    }
}