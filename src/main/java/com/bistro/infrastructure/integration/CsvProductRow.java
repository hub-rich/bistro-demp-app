package com.bistro.infrastructure.integration;

import com.bistro.domain.model.ProductCategory;

import java.math.BigDecimal;

record CsvProductRow(String name, BigDecimal price, ProductCategory category) {
}
