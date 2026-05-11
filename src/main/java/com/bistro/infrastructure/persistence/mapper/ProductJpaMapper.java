package com.bistro.infrastructure.persistence.mapper;

import com.bistro.domain.model.Product;
import com.bistro.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductJpaMapper {

    public ProductJpaEntity toEntity(Product product) {
        return new ProductJpaEntity(product.getName(), product.getPrice(), product.getCategory());
    }

    public Product fromEntity(ProductJpaEntity entity) {
        return Product.reconstitute(entity.getId(), entity.getName(), entity.getPrice(), entity.getCategory());
    }
}
