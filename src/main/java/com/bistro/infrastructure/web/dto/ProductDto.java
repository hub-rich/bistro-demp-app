package com.bistro.infrastructure.web.dto;

import com.bistro.domain.model.ProductCategory;

import java.math.BigDecimal;

public record ProductDto(Long id, String name, BigDecimal price, ProductCategory category) {
}
