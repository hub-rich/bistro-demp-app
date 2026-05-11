package com.bistro.infrastructure.integration;

import com.bistro.domain.model.Product;
import com.bistro.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
class CsvImportIntegrationTest {

    @TempDir
    static Path importDir;

    @DynamicPropertySource
    static void configureImportDirectory(DynamicPropertyRegistry registry) {
        registry.add("bistro.csv.import.directory", importDir::toString);
        registry.add("bistro.csv.poll.interval.ms", () -> "500");
    }

    @Autowired
    private ProductRepository productRepository;

    @Test
    void droppingCsvFile_importsProducts() throws IOException {
        var csv = importDir.resolve("test-products.csv");
        Files.writeString(csv, "name,price,category\nTestPizza,12.00,PIZZA\nTestDrink,3.00,DRINK\n");

        await().atMost(15, SECONDS).until(() ->
            productsByName("TestPizza") > 0 && productsByName("TestDrink") > 0);

        assertThat(productRepository.findAll())
            .extracting(Product::getName)
            .contains("TestPizza", "TestDrink");
    }

    @Test
    void droppingSameFileAgain_doesNotDuplicateImport() throws IOException {
        var csv = importDir.resolve("duplicate-test.csv");
        Files.writeString(csv, "name,price,category\nDupProduct,5.00,SNACK\n");

        await().atMost(15, SECONDS).until(() -> productsByName("DupProduct") > 0);

        var countBefore = productsByName("DupProduct");

        csv.toFile().setLastModified(System.currentTimeMillis());

        await().during(2, SECONDS).atMost(5, SECONDS)
            .until(() -> productsByName("DupProduct") == countBefore);
    }

    private long productsByName(String name) {
        return productRepository.findAll().stream()
            .filter(p -> name.equals(p.getName()))
            .count();
    }
}
