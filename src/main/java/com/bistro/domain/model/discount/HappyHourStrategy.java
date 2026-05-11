package com.bistro.domain.model.discount;

import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.ProductCategory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Collections;

@RequiredArgsConstructor
public class HappyHourStrategy implements DiscountStrategy {

    private static final LocalTime START = LocalTime.of(17, 0);
    private static final LocalTime END = LocalTime.of(19, 0);

    private final Clock clock;

    @Override
    public DiscountPhase phase() {
        return DiscountPhase.ITEM_LEVEL;
    }

    @Override
    public boolean isApplicable(Order order, BigDecimal currentTotal) {
        var now = LocalTime.now(clock);
        if (now.isBefore(START) || !now.isBefore(END)) {
            return false;
        }
        var pizzaUnitCount = order.getItems().stream()
                .filter(item -> item.product().getCategory() == ProductCategory.PIZZA)
                .mapToInt(OrderItem::quantity)
                .sum();
        return pizzaUnitCount >= 2;
    }

    @Override
    public Discount apply(Order order, BigDecimal currentTotal) {
        var sortedPizzaPrices = order.getItems().stream()
                .filter(item -> item.product().getCategory() == ProductCategory.PIZZA)
                .flatMap(item -> Collections.nCopies(item.quantity(), item.product().getPrice()).stream())
                .sorted()
                .toList();

        var freeCount = sortedPizzaPrices.size() / 2;
        var discount = sortedPizzaPrices.stream()
                .limit(freeCount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Discount("Happy Hour (2for1)", discount);
    }
}
