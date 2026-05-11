package com.bistro.domain.model.discount;

import com.bistro.domain.model.Order;
import java.math.BigDecimal;

public interface DiscountStrategy {
    DiscountPhase phase();
    boolean isApplicable(Order order, BigDecimal currentTotal);
    Discount apply(Order order, BigDecimal currentTotal);
}
