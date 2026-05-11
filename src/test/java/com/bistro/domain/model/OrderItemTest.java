package com.bistro.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    void calculateSubtotal_returnsQuantityTimesPrice() {
        var item = new OrderItem(Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA), 3);

        assertThat(item.calculateSubtotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void calculateSubtotal_withSingleUnit_returnsSinglePrice() {
        var item = new OrderItem(Product.create("Tiramisu", new BigDecimal("5.50"), ProductCategory.DESSERT), 1);

        assertThat(item.calculateSubtotal()).isEqualByComparingTo("5.50");
    }
}
