package com.bistro.domain.model;

import java.math.BigDecimal;

public record OrderItem(Product product, int quantity) {

    public BigDecimal calculateSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
