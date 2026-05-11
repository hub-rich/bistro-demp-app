package com.bistro.infrastructure.integration;

import com.bistro.application.service.ProductService;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCsvImportHandlerTest {

    @TempDir
    Path tempDir;

    @Mock
    ProductService productService;

    @InjectMocks
    ProductCsvImportHandler sut;


    @Test
    void handle_savesAllValidRows() throws IOException {
        var csv = tempDir.resolve("products.csv");
        Files.writeString(csv, "name,price,category\nMargherita,10.00,PIZZA\nCola,2.50,DRINK\n");

        sut.handle(csv.toFile());

        verify(productService, times(2)).save(any(Product.class));
    }

    @Test
    void handle_skipsInvalidRow_andContinuesWithRemainingRows() throws IOException {
        when(productService.save(any()))
                .thenThrow(new RuntimeException("save failed"))
                .thenReturn(Product.create("Cola", new BigDecimal("2.50"), ProductCategory.DRINK));
        var csv = tempDir.resolve("products.csv");
        Files.writeString(csv, "name,price,category\nMargherita,10.00,PIZZA\nCola,2.50,DRINK\n");

        sut.handle(csv.toFile());

        verify(productService, times(2)).save(any(Product.class));
    }

    @Test
    void handle_throwsCsvImportException_whenFileCannotBeRead() {
        var nonExistent = new File("nonexistent.csv");

        assertThatThrownBy(() -> sut.handle(nonExistent))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("nonexistent.csv");
    }
}
