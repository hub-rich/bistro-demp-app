package com.bistro.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Product {

    private final Long id;
    private final String name;
    private final BigDecimal price;
    private final ProductCategory category;

    public static Product create(String name, BigDecimal price, ProductCategory category) {
        return new Product(null, name, price, category);
    }

    public static Product reconstitute(Long id, String name, BigDecimal price, ProductCategory category) {
        return new Product(id, name, price, category);
    }
}
