package com.bistro.infrastructure.web.mapper;

import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    void toDto_mapsAllFields() {
        var product = Product.reconstitute(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);

        var dto = mapper.toDto(product);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Margherita");
        assertThat(dto.price()).isEqualByComparingTo("10.00");
        assertThat(dto.category()).isEqualTo(ProductCategory.PIZZA);
    }
}
