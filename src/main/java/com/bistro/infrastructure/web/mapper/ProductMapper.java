package com.bistro.infrastructure.web.mapper;

import com.bistro.infrastructure.web.dto.ProductDto;
import com.bistro.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getCategory());
    }
}
