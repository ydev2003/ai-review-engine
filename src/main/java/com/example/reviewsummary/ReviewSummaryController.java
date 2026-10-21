package com.example.reviewsummary;

import com.example.reviewsummary.processor.ReviewProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/review-summaries")
public class ReviewSummaryController {

    private final ReviewProcessor processor;

    public ReviewSummaryController(ReviewProcessor processor) {
        this.processor = processor;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateSummaries() {
        try {
            processor.processReviews();
            return ResponseEntity.ok(Map.of("message", "Successfully processed review summaries"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }
}
