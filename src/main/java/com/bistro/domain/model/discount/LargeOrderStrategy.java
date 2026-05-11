package com.bistro.domain.model.discount;

import com.bistro.domain.model.Order;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LargeOrderStrategy implements DiscountStrategy {

    private static final BigDecimal THRESHOLD = new BigDecimal("40.00");
    private static final BigDecimal RATE = new BigDecimal("0.10");

    @Override
    public DiscountPhase phase() {
        return DiscountPhase.ORDER_LEVEL;
    }

    @Override
    public boolean isApplicable(Order order, BigDecimal currentTotal) {
        return currentTotal.compareTo(THRESHOLD) >= 0;
    }

    @Override
    public Discount apply(Order order, BigDecimal currentTotal) {
        var amount = currentTotal.multiply(RATE).setScale(2, RoundingMode.HALF_UP);
        return new Discount("Large Order (10%)", amount);
    }
}
