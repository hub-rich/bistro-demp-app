package com.bistro.domain.model;

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

class OrderTest {

    private Product pizza() {
        return Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
    }

    @Test
    void create_setsSubtotalAndTotalFromItems() {
        var order = Order.create(1, LocalDateTime.now(),
            List.of(new OrderItem(pizza(), 2)),
            List.of());

        assertThat(order.getSubtotal()).isEqualByComparingTo("20.00");
        assertThat(order.getTotal()).isEqualByComparingTo("20.00");
        assertThat(order.getDiscounts()).isEmpty();
    }

    @Test
    void create_multipleItems_accumulatesSubtotal() {
        var cola = Product.create("Cola", new BigDecimal("2.50"), ProductCategory.DRINK);

        var order = Order.create(1, LocalDateTime.now(),
            List.of(new OrderItem(pizza(), 1), new OrderItem(cola, 3)),
            List.of());

        assertThat(order.getSubtotal()).isEqualByComparingTo("17.50");
    }

    @Test
    void create_appliesApplicableDiscounts() {
        var strategy = mock(DiscountStrategy.class);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any()))
            .thenReturn(new Discount("Happy Hour (2for1)", new BigDecimal("10.00")));

        var order = Order.create(1, LocalDateTime.now(),
            List.of(new OrderItem(pizza(), 2)),
            List.of(strategy));

        assertThat(order.getTotal()).isEqualByComparingTo("10.00");
        assertThat(order.getDiscounts()).hasSize(1);
        assertThat(order.getDiscounts().getFirst().description()).isEqualTo("Happy Hour (2for1)");
    }
}
