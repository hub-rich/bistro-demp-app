package com.bistro.infrastructure.persistence;

import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.bistro.infrastructure.persistence.mapper.ProductJpaMapper;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ProductRepositoryAdapter.class, ProductJpaMapper.class})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void save_and_findById_returnsProduct() {
        var saved = productRepository.save(
                Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA));

        assertThat(productRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Product::getName)
                .isEqualTo("Margherita");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        assertThat(productRepository.findById(999L)).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedProducts() {
        productRepository.save(Product.create("Cola", new BigDecimal("2.50"), ProductCategory.DRINK));
        productRepository.save(Product.create("Tiramisu", new BigDecimal("4.00"), ProductCategory.DESSERT));

        var all = productRepository.findAll();
        assertThat(all).hasSize(2);
    }
}
