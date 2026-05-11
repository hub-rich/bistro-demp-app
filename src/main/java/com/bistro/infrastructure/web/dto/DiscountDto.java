package com.bistro.infrastructure.web.dto;

import java.math.BigDecimal;

public record DiscountDto(String description, BigDecimal amount) {
}
