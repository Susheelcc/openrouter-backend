package com.open.openrouter.client;

import com.open.openrouter.dto.OpenRouterRequest;
import com.open.openrouter.dto.OpenRouterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

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
            String upstreamBody = e.getResponseBodyAsString();

            if (e.getStatusCode().value() == 401) {
                log.error("OpenRouter 401 Unauthorized | body={}", upstreamBody);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "OpenRouter rejected the API key (401). Check OPENROUTER_API_KEY."
                );
            }

            if (e.getStatusCode().value() == 402) {
                log.error("OpenRouter 402 Payment Required | body={}", upstreamBody);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "OpenRouter credits unavailable (402). Check billing/credits."
                );
            }

            log.error("OpenRouter HTTP error: {} | body={}", e.getStatusCode(), upstreamBody);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "OpenRouter error: " + e.getStatusCode().value()
            );

        } catch (Exception e) {

            // ---- Timeout / network / unknown errors ----
            log.error("OpenRouter call failed", e);

            return null; // fallback handled in service
        }
    }
}
