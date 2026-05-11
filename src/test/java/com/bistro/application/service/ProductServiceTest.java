package com.bistro.application.service;

import com.bistro.application.exception.ProductNotFoundException;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductService service = new ProductService(repository);

    private Product pizza() {
        return Product.reconstitute(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
    }

    @Test
    void findAll_returnsAllProductsFromRepository() {
        var products = List.of(pizza());
        when(repository.findAll()).thenReturn(products);

        assertThat(service.findAll()).isSameAs(products);
    }

    @Test
    void findById_returnsProduct_whenFound() {
        var product = pizza();
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        assertThat(service.findById(1L)).isSameAs(product);
    }

    @Test
    void findById_throwsProductNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void save_delegatesToRepository() {
        var product = Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
        var saved = pizza();
        when(repository.save(product)).thenReturn(saved);

        assertThat(service.save(product)).isSameAs(saved);
    }
}
