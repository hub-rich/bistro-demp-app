package com.bistro.infrastructure.integration;

public class CsvImportException extends RuntimeException {
    public CsvImportException(String filename, Throwable cause) {
        super("Failed to import CSV file: " + filename, cause);
    }
}
