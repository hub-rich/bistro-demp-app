package com.bistro.domain.discount;

import com.bistro.domain.model.Order;
import com.bistro.domain.model.discount.LargeOrderStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LargeOrderStrategyTest {

    private final LargeOrderStrategy strategy = new LargeOrderStrategy();

    private Order emptyOrder() {
        return Order.create(1, LocalDateTime.now(), List.of(), List.of());
    }

    @Test
    void isApplicable_returnsTrue_whenTotalEqualsThreshold() {
        assertThat(strategy.isApplicable(emptyOrder(), new BigDecimal("40.00"))).isTrue();
    }

    @Test
    void isApplicable_returnsTrue_whenTotalAboveThreshold() {
        assertThat(strategy.isApplicable(emptyOrder(), new BigDecimal("50.00"))).isTrue();
    }

    @Test
    void isApplicable_returnsFalse_whenTotalBelowThreshold() {
        assertThat(strategy.isApplicable(emptyOrder(), new BigDecimal("39.99"))).isFalse();
    }

    @Test
    void apply_returns10PercentOfCurrentTotal() {
        var result = strategy.apply(emptyOrder(), new BigDecimal("50.00"));
        assertThat(result.amount()).isEqualByComparingTo("5.00");
        assertThat(result.description()).isEqualTo("Large Order (10%)");
    }

    @Test
    void apply_roundsUp() {
        var result = strategy.apply(emptyOrder(), new BigDecimal("41.67"));
        assertThat(result.amount()).isEqualByComparingTo("4.17");
    }

    @Test
    void apply_roundsDown() {
        var result = strategy.apply(emptyOrder(), new BigDecimal("41.62"));
        assertThat(result.amount()).isEqualByComparingTo("4.16");
    }
}
