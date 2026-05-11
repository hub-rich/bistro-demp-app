package com.bistro.infrastructure.web;

import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.model.discount.Discount;
import com.bistro.domain.model.discount.DiscountStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void generateReceipt_formatsAllSections() {
        var pizza = Product.reconstitute(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
        var discount = new Discount("Happy Hour (2for1)", new BigDecimal("10.00"));
        var strategy = mock(DiscountStrategy.class);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any())).thenReturn(discount);
        var order = Order.create(10, LocalDateTime.of(2026, 5, 9, 18, 0),
            List.of(new OrderItem(pizza, 2)),
            List.of(strategy));

        var receipt = formatter.generateReceipt(order);

        assertThat(receipt).contains("Table Nr. 10")
            .contains("2 x Margherita @ 10.0 = 20.0")
            .contains("Subtotal: 20.0")
            .contains("Happy Hour (2for1): -10.0")
            .contains("Total: 10.0");
    }

    @Test
    void generateReceipt_noDiscounts_showsSubtotalEqualsTotal() {
        var drink = Product.reconstitute(2L, "Cola", new BigDecimal("2.50"), ProductCategory.DRINK);
        var order = Order.create(3, LocalDateTime.now(),
            List.of(new OrderItem(drink, 1)),
            List.of());

        var receipt = formatter.generateReceipt(order);

        assertThat(receipt).contains("Subtotal: 2.5")
            .contains("Total: 2.5")
            .doesNotContain(": -");
    }

    @Test
    void generateReceipt_noItems_showsZeroTotals() {
        var order = Order.create(7, LocalDateTime.now(), List.of(), List.of());

        var receipt = formatter.generateReceipt(order);

        assertThat(receipt).contains("Table Nr. 7")
            .contains("Subtotal: 0.0")
            .contains("Total: 0.0")
            .doesNotContain(": -");
    }
}
