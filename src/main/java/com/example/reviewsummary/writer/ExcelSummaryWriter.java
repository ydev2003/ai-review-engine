package com.example.reviewsummary.writer;

import com.example.reviewsummary.model.ProductSummary;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelSummaryWriter {

    public void writeSummaries(List<ProductSummary> summaries, String outputPath) {
        java.io.File outputFile = new java.io.File(outputPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(outputFile)) {

            Sheet sheet = workbook.createSheet("Summaries");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("product_id");
            headerRow.createCell(1).setCellValue("summary");

            int rowNum = 1;
            for (ProductSummary summary : summaries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(summary.productId());
                row.createCell(1).setCellValue(summary.summary());
            }

            workbook.write(out);

        } catch (IOException e) {
            throw new RuntimeException("Error writing Excel file: " + outputPath, e);
        }
    }
}
