package com.bistro.domain.discount;

import com.bistro.domain.model.*;
import com.bistro.domain.model.discount.HappyHourStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HappyHourStrategyTest {

    private static Clock clockAt(int hour, int minute) {
        return Clock.fixed(
                LocalDate.of(2026, 5, 9).atTime(hour, minute).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC);
    }

    private static Order orderWithPizzas(int... quantities) {
        var items = new java.util.ArrayList<OrderItem>();
        var price = 10;
        for (var qty : quantities) {
            var p = Product.create("Pizza" + price, new BigDecimal(price), ProductCategory.PIZZA);
            items.add(new OrderItem(p, qty));
            price += 2;
        }
        return Order.create(1, LocalDateTime.now(), items, List.of());
    }

    @Test
    void isApplicable_returnsTrue_duringHappyHour() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        assertThat(strategy.isApplicable(orderWithPizzas(2), new BigDecimal("20.00"))).isTrue();
    }

    @Test
    void isApplicable_returnsFalse_beforeHappyHour() {
        var strategy = new HappyHourStrategy(clockAt(16, 59));
        assertThat(strategy.isApplicable(orderWithPizzas(2), new BigDecimal("20.00"))).isFalse();
    }

    @Test
    void isApplicable_returnsFalse_afterHappyHour() {
        var strategy = new HappyHourStrategy(clockAt(19, 0));
        assertThat(strategy.isApplicable(orderWithPizzas(2), new BigDecimal("20.00"))).isFalse();
    }

    @Test
    void isApplicable_returnsFalse_duringHappyHour_whenNoPizzasInOrder() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        var order = Order.create(1, LocalDateTime.now(),
            List.of(new OrderItem(Product.create("Cola", new BigDecimal("2.50"), ProductCategory.DRINK), 4)),
            List.of());
        assertThat(strategy.isApplicable(order, new BigDecimal("10.00"))).isFalse();
    }

    @Test
    void isApplicable_returnsFalse_duringHappyHour_whenOnlyOnePizzaUnit() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        assertThat(strategy.isApplicable(orderWithPizzas(1), new BigDecimal("10.00"))).isFalse();
    }

    @Test
    void apply_givesCheapestPizzaFree_forTwoPizzasSameProduct() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        var result = strategy.apply(orderWithPizzas(2), new BigDecimal("20.00"));
        assertThat(result.amount()).isEqualByComparingTo("10.00");
        assertThat(result.description()).isEqualTo("Happy Hour (2for1)");
    }

    @Test
    void apply_givesCheaperPizzaFree_forTwoDifferentPizzas() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        var result = strategy.apply(orderWithPizzas(1, 1), new BigDecimal("22.00"));
        assertThat(result.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void apply_handlesOddQuantity_floorDivision() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        var result = strategy.apply(orderWithPizzas(3), new BigDecimal("30.00"));
        assertThat(result.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void apply_givesTwoCheapestFree_forFourPizzasSameProduct() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        var result = strategy.apply(orderWithPizzas(4), new BigDecimal("40.00"));
        assertThat(result.amount()).isEqualByComparingTo("20.00");
        assertThat(result.description()).isEqualTo("Happy Hour (2for1)");
    }

    @Test
    void apply_givesTwoCheapestFree_forFourDifferentPizzas() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        // prices: 10, 12, 14, 16 — cheapest two (10 + 12) are free
        var result = strategy.apply(orderWithPizzas(1, 1, 1, 1), new BigDecimal("52.00"));
        assertThat(result.amount()).isEqualByComparingTo("22.00");
    }

    @Test
    void apply_ignoresNonPizzaItems() {
        var strategy = new HappyHourStrategy(clockAt(18, 0));
        var order = Order.create(1, LocalDateTime.now(),
            List.of(
                new OrderItem(Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA), 2),
                new OrderItem(Product.create("Cola", new BigDecimal("2.50"), ProductCategory.DRINK), 2)
            ),
            List.of());

        var result = strategy.apply(order, new BigDecimal("25.00"));
        assertThat(result.amount()).isEqualByComparingTo("10.00");
    }
}
