package com.bistro.infrastructure.web.dto;

import java.math.BigDecimal;

public record OrderItemDto(String productName, int quantity, BigDecimal unitPrice) {}
