package com.bistro.application.service;

import com.bistro.application.exception.OrderNotFoundException;
import com.bistro.application.exception.OrderValidationException;
import com.bistro.application.service.command.OrderCommand;
import com.bistro.application.service.command.OrderItemCommand;
import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.discount.DiscountStrategy;
import com.bistro.domain.repository.OrderRepository;
import com.bistro.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final List<DiscountStrategy> discountStrategies;
    private final Clock clock;

    public Order createOrder(OrderCommand command) {
        validateCommand(command);

        var items = command.items().stream()
            .map(this::toOrderItem)
            .toList();

        var order = Order.create(command.tableNumber(), LocalDateTime.now(clock), items, discountStrategies);

        return orderRepository.save(order);
    }

    private static void validateCommand(OrderCommand command) {
        if (command.tableNumber() == null) {
            throw new OrderValidationException("tableNumber is required");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new OrderValidationException("Order must contain at least one item");
        }
    }

    private OrderItem toOrderItem(OrderItemCommand item) {
        var product = productRepository.findById(item.productId())
            .orElseThrow(() -> new OrderValidationException("Product with id '" + item.productId() + "' not found"));
        return new OrderItem(product, item.quantity());
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
