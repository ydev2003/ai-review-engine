# AI Review Engine

This project is a standalone backend processing engine designed to read Excel files containing reviews, process them using an AI Large Language Model (LLM) to generate summaries, and write the results back to a new Excel file.

## Purpose
The primary goal is to provide a reliable, testable, and maintainable pipeline for processing bulk text reviews. This application is completely standalone and does not include any web interfaces or databases. It reads from and writes to the local filesystem.

## Prerequisites
- Java 21
- Maven 3.8+
- An API Key for the chosen LLM provider (to be implemented in future phases)

## Folder Structure

- `src/main/java/com/example/reviewsummary/`: Contains the Java source code for the application.
  - `config/`: Application configuration classes.
  - `model/`: Data models and DTOs representing reviews and summaries.
  - `reader/`: Components responsible for reading input Excel files.
  - `processor/`: The core processing logic coordinating the read, summarize, and write steps.
  - `service/`: General business logic services.
  - `llm/`: Components handling interaction with the external LLM API.
  - `writer/`: Components responsible for writing output Excel files.
  - `exception/`: Custom exceptions for error handling.
- `src/main/resources/`: Configuration files like `application.yml`.
- `input/`: Directory where the input Excel files containing reviews should be placed.
- `output/`: Directory where the generated Excel files with summaries will be saved.

## Setup

1. Copy `.env.example` to `.env` and fill in the required configurations when applicable.
2. Ensure you have the required input Excel file in the `input/` directory.

## Build and Run

To build the application and run the tests:
```bash
mvn clean test
```

To run the application:
```bash
mvn spring-boot:run
```
*(Note: Application functionality is pending as this repository is prepared for the next implementation phase.)*

## Future Implementation
The current state sets up the basic foundation. The next phase involves:
1. Implementing the Excel reader using Apache POI.
2. Integrating the LLM API for review summarization.
3. Implementing the Excel writer to output the results.
