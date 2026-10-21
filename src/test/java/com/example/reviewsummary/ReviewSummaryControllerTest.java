package com.example.reviewsummary;

import com.example.reviewsummary.processor.ReviewProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewSummaryController.class)
class ReviewSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewProcessor processor;

    @Test
    void generateSummaries_success() throws Exception {
        doNothing().when(processor).processReviews();

        mockMvc.perform(post("/api/review-summaries/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully processed review summaries"));
    }

    @Test
    void generateSummaries_error() throws Exception {
        doThrow(new RuntimeException("Processing failed")).when(processor).processReviews();

        mockMvc.perform(post("/api/review-summaries/generate"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Processing failed"));
    }
}
