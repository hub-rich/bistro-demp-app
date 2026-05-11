package com.bistro.infrastructure.integration;

import com.bistro.application.service.ProductService;
import com.bistro.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectReader;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.io.File;

@Component
@RequiredArgsConstructor
public class ProductCsvImportHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductCsvImportHandler.class);

    private final ProductService productService;
    private final ObjectReader reader =
        new CsvMapper().readerFor(CsvProductRow.class).with(CsvSchema.emptySchema().withHeader());

    public void handle(File file) {
        try (var values = reader.<CsvProductRow>readValues(file)) {
            values.forEachRemaining(row -> trySaveProduct(row, file.getName()));
        } catch (Exception ex) {
            throw new CsvImportException(file.getName(), ex);
        }
    }

    private void trySaveProduct(CsvProductRow row, String filename) {
        try {
            productService.save(Product.create(row.name(), row.price(), row.category()));
        } catch (Exception ex) {
            log.warn("[{}] Skipping invalid CSV row: {}", filename, ex.getMessage());
        }
    }
}
