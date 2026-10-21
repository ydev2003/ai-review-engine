package com.example.reviewsummary.llm;

import com.example.reviewsummary.model.GeminiRequest;
import com.example.reviewsummary.model.GeminiResponse;
import com.example.reviewsummary.model.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String endpoint;

    public GeminiClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.llm.api-key}") String apiKey,
            @Value("${app.llm.model}") String model,
            @Value("${app.llm.endpoint}") String endpoint) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    public String generateSummary(String productId, List<Review> reviews) {
        String prompt = buildPrompt(productId, reviews);

        GeminiRequest request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))
        ));

        String url = String.format("%s%s:generateContent?key=%s", endpoint, model, apiKey);

        GeminiResponse response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);

        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
            return response.candidates().get(0).content().parts().get(0).text().trim();
        }

        throw new RuntimeException("Failed to generate summary from Gemini API for product " + productId);
    }

    private String buildPrompt(String productId, List<Review> reviews) {
        String reviewTexts = reviews.stream()
                .map(r -> "- " + r.reviewText())
                .collect(Collectors.joining("\n"));

        return String.format(
                "Please provide a 2-3 sentence summary of the following reviews for product ID %s:\n\n%s",
                productId, reviewTexts
        );
    }
}
