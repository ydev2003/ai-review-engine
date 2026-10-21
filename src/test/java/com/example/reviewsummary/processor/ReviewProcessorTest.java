package com.example.reviewsummary.processor;

import com.example.reviewsummary.llm.GeminiClient;
import com.example.reviewsummary.model.ProductSummary;
import com.example.reviewsummary.model.Review;
import com.example.reviewsummary.reader.ExcelReviewReader;
import com.example.reviewsummary.writer.ExcelSummaryWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewProcessorTest {

    @Mock
    private ExcelReviewReader reader;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ExcelSummaryWriter writer;

    @Captor
    private ArgumentCaptor<List<ProductSummary>> summaryCaptor;

    private ReviewProcessor processor;

    private final String inputPath = "dummy_input.xlsx";
    private final String outputPath = "dummy_output.xlsx";
    private final String worksheetName = "Reviews_10000";

    @BeforeEach
    void setUp() {
        processor = new ReviewProcessor(reader, geminiClient, writer, inputPath, outputPath, worksheetName);
    }

    @Test
    void processReviews_success() {
        Review r1 = new Review("P1", "Product 1", "Cat", 5, "Good", "Text 1", LocalDate.now(), true, 0, "User1", "en", "v1");
        Review r2 = new Review("P1", "Product 1", "Cat", 4, "Okay", "Text 2", LocalDate.now(), true, 0, "User2", "en", "v1");
        Review r3 = new Review("P2", "Product 2", "Cat", 1, "Bad", "Text 3", LocalDate.now(), true, 0, "User3", "en", "v1");

        when(reader.readReviews(inputPath, worksheetName)).thenReturn(List.of(r1, r2, r3));
        when(geminiClient.generateSummary(eq("P1"), any())).thenReturn("Summary for P1");
        when(geminiClient.generateSummary(eq("P2"), any())).thenReturn("Summary for P2");

        processor.processReviews();

        verify(reader).readReviews(inputPath, worksheetName);
        verify(geminiClient, times(2)).generateSummary(anyString(), anyList());
        verify(writer).writeSummaries(summaryCaptor.capture(), eq(outputPath));

        List<ProductSummary> capturedSummaries = summaryCaptor.getValue();
        assertEquals(2, capturedSummaries.size());

        long p1Count = capturedSummaries.stream().filter(s -> s.productId().equals("P1") && s.summary().equals("Summary for P1")).count();
        assertEquals(1, p1Count);
    }

    @Test
    void processReviews_skipFailingProduct() {
        Review r1 = new Review("P1", "Product 1", "Cat", 5, "Good", "Text 1", LocalDate.now(), true, 0, "User1", "en", "v1");
        Review r2 = new Review("P2", "Product 2", "Cat", 1, "Bad", "Text 2", LocalDate.now(), true, 0, "User2", "en", "v1");

        when(reader.readReviews(inputPath, worksheetName)).thenReturn(List.of(r1, r2));
        when(geminiClient.generateSummary(eq("P1"), any())).thenThrow(new RuntimeException("API Error"));
        when(geminiClient.generateSummary(eq("P2"), any())).thenReturn("Summary for P2");

        processor.processReviews();

        verify(writer).writeSummaries(summaryCaptor.capture(), eq(outputPath));

        List<ProductSummary> capturedSummaries = summaryCaptor.getValue();
        assertEquals(1, capturedSummaries.size());
        assertEquals("P2", capturedSummaries.get(0).productId());
        assertEquals("Summary for P2", capturedSummaries.get(0).summary());
    }
}
