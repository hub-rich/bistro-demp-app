package com.bistro.domain.model;

import com.bistro.domain.model.discount.Discount;
import com.bistro.domain.model.discount.DiscountStrategy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Order {

    private final Long id;
    private final Integer tableNumber;
    private final LocalDateTime orderTime;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private final List<OrderItem> items = new ArrayList<>();
    private final List<Discount> discounts = new ArrayList<>();

    public static Order create(Integer tableNumber,
                               LocalDateTime orderTime,
                               List<OrderItem> items,
                               List<DiscountStrategy> strategies) {
        var order = new Order(null, tableNumber, orderTime);

        order.items.addAll(items);
        order.subtotal = items.stream()
            .map(OrderItem::calculateSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.total = order.subtotal;

        var sortedStrategies = strategies.stream()
            .sorted(Comparator.comparingInt(s -> s.phase().priority()))
            .toList();
        for (var strategy : sortedStrategies) {
            if (strategy.isApplicable(order, order.total)) {
                var discount = strategy.apply(order, order.total);
                order.applyDiscount(discount);
            }
        }
        return order;
    }

    private void applyDiscount(Discount discount) {
        discounts.add(discount);
        total = total.subtract(discount.amount());
    }

    public static Order reconstitute(Long id, Integer tableNumber, LocalDateTime orderTime,
                                     BigDecimal subtotal, BigDecimal total,
                                     List<OrderItem> items, List<Discount> discounts) {
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(discounts, "discounts must not be null");
        var order = new Order(id, tableNumber, orderTime);
        order.subtotal = subtotal;
        order.total = total;
        order.items.addAll(items);
        order.discounts.addAll(discounts);
        return order;
    }

}
