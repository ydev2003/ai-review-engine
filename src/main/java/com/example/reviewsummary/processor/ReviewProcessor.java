package com.example.reviewsummary.processor;

import com.example.reviewsummary.llm.GeminiClient;
import com.example.reviewsummary.model.ProductSummary;
import com.example.reviewsummary.model.Review;
import com.example.reviewsummary.reader.ExcelReviewReader;
import com.example.reviewsummary.writer.ExcelSummaryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewProcessor {

    private static final Logger log = LoggerFactory.getLogger(ReviewProcessor.class);

    private final ExcelReviewReader reader;
    private final GeminiClient geminiClient;
    private final ExcelSummaryWriter writer;
    private final String inputPath;
    private final String outputPath;
    private final String worksheetName;

    public ReviewProcessor(
            ExcelReviewReader reader,
            GeminiClient geminiClient,
            ExcelSummaryWriter writer,
            @Value("${app.io.input-path}") String inputPath,
            @Value("${app.io.output-path}") String outputPath,
            @Value("${app.io.worksheet-name}") String worksheetName) {
        this.reader = reader;
        this.geminiClient = geminiClient;
        this.writer = writer;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.worksheetName = worksheetName;
    }

    public void processReviews() {
        log.info("Starting review processing pipeline.");

        // 1. Read and validate reviews
        List<Review> reviews = reader.readReviews(inputPath, worksheetName);
        log.info("Successfully read {} reviews from Excel.", reviews.size());

        // 2. Group reviews by product ID
        Map<String, List<Review>> groupedReviews = reviews.stream()
                .collect(Collectors.groupingBy(Review::productId));
        log.info("Grouped into {} unique products.", groupedReviews.size());

        // 3. Generate summaries
        List<ProductSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<Review>> entry : groupedReviews.entrySet()) {
            String productId = entry.getKey();
            try {
                String summaryText = geminiClient.generateSummary(productId, entry.getValue());
                summaries.add(new ProductSummary(productId, summaryText));
                log.debug("Successfully generated summary for product {}", productId);
            } catch (Exception e) {
                log.error("Failed to generate summary for product {}. Skipping.", productId, e);
            }
        }

        // 4. Write summaries to output Excel
        writer.writeSummaries(summaries, outputPath);
        log.info("Successfully wrote {} summaries to {}.", summaries.size(), outputPath);
    }
}
