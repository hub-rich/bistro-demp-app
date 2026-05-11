package com.bistro.infrastructure.persistence.mapper;

import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.infrastructure.persistence.entity.ProductJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductJpaMapperTest {

    private final ProductJpaMapper mapper = new ProductJpaMapper();

    @Test
    void toEntity_mapsAllFields() {
        var product = Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);

        var entity = mapper.toEntity(product);

        assertThat(entity.getName()).isEqualTo("Margherita");
        assertThat(entity.getPrice()).isEqualByComparingTo("10.00");
        assertThat(entity.getCategory()).isEqualTo(ProductCategory.PIZZA);
    }

    @Test
    void fromEntity_mapsAllFieldsIncludingId() {
        var entity = new ProductJpaEntity("Cola", new BigDecimal("2.50"), ProductCategory.DRINK);
        ReflectionTestUtils.setField(entity, "id", 42L);

        var product = mapper.fromEntity(entity);

        assertThat(product.getId()).isEqualTo(42L);
        assertThat(product.getName()).isEqualTo("Cola");
        assertThat(product.getPrice()).isEqualByComparingTo("2.50");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.DRINK);
    }
}
