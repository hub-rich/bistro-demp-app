package com.bistro.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
    Long id,
    Integer tableNumber,
    LocalDateTime orderTime,
    List<OrderItemDto> items,
    BigDecimal subtotal,
    List<DiscountDto> discounts,
    BigDecimal total,
    String receipt
) {
}
