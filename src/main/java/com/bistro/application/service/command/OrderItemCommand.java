package com.bistro.application.service.command;

public record OrderItemCommand(Long productId, int quantity) {}
