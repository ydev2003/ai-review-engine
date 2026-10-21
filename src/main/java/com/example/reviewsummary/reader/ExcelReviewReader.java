package com.example.reviewsummary.reader;

import com.example.reviewsummary.model.Review;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class ExcelReviewReader {

    public List<Review> readReviews(String filePath, String sheetName) {
        List<Review> reviews = new ArrayList<>();

        try (InputStream is = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                throw new IllegalArgumentException("Excel file is empty.");
            }

            Row headerRow = rowIterator.next();
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING) {
                    headerMap.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
                }
            }

            Integer productIdCol = headerMap.get("product_id");
            Integer reviewTextCol = headerMap.get("review_text");

            if (productIdCol == null || reviewTextCol == null) {
                throw new IllegalArgumentException("Missing essential columns: product_id or review_text");
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String productId = getStringValue(row, productIdCol);
                String reviewText = getStringValue(row, reviewTextCol);

                if (productId == null || productId.trim().isEmpty() || reviewText == null || reviewText.trim().isEmpty()) {
                    continue; // Skip rows with missing essential data
                }

                Review review = new Review(
                        productId.trim(),
                        getStringValue(row, headerMap.get("product_name")),
                        getStringValue(row, headerMap.get("category")),
                        getIntegerValue(row, headerMap.get("rating")),
                        getStringValue(row, headerMap.get("review_title")),
                        reviewText.trim(),
                        getDateValue(row, headerMap.get("review_date")),
                        getBooleanValue(row, headerMap.get("verified_purchase")),
                        getIntegerValue(row, headerMap.get("helpful_votes")),
                        getStringValue(row, headerMap.get("reviewer_name")),
                        getStringValue(row, headerMap.get("locale")),
                        getStringValue(row, headerMap.get("variant"))
                );
                reviews.add(review);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file: " + filePath, e);
        }

        return reviews;
    }

    private String getStringValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private Integer getIntegerValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Boolean getBooleanValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim().toLowerCase();
            return "true".equals(val) || "yes".equals(val) || "1".equals(val);
        }
        return null;
    }

    private LocalDate getDateValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
             return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null; // Basic handling, could be expanded for string parsing if needed
    }
}
