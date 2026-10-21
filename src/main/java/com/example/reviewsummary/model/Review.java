package com.example.reviewsummary.model;

import java.time.LocalDate;

public record Review(
        String productId,
        String productName,
        String category,
        Integer rating,
        String reviewTitle,
        String reviewText,
        LocalDate reviewDate,
        Boolean verifiedPurchase,
        Integer helpfulVotes,
        String reviewerName,
        String locale,
        String variant
) {}
