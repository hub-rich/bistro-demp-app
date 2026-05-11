package com.bistro.application.service.command;

import java.util.List;

public record OrderCommand(Integer tableNumber, List<OrderItemCommand> items) {}
