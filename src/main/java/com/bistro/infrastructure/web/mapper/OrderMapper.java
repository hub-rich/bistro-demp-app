package com.bistro.infrastructure.web.mapper;

import com.bistro.infrastructure.web.dto.DiscountDto;
import com.bistro.infrastructure.web.dto.OrderDto;
import com.bistro.infrastructure.web.dto.OrderItemDto;
import com.bistro.infrastructure.web.dto.request.OrderRequest;
import com.bistro.application.service.command.OrderCommand;
import com.bistro.application.service.command.OrderItemCommand;
import com.bistro.domain.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderCommand toCommand(OrderRequest request) {
        var items = request.items().stream()
            .map(i -> new OrderItemCommand(i.productId(), i.quantity()))
            .toList();
        return new OrderCommand(request.tableNumber(), items);
    }

    public OrderDto toDto(Order order, String receipt) {
        var items = mapItems(order);
        var discounts = mapDiscounts(order);

        return new OrderDto(
            order.getId(),
            order.getTableNumber(),
            order.getOrderTime(),
            items,
            order.getSubtotal(),
            discounts,
            order.getTotal(),
            receipt);
    }

    private List<OrderItemDto> mapItems(Order order) {
        return order.getItems().stream()
            .map(item -> new OrderItemDto(
                item.product().getName(),
                item.quantity(),
                item.product().getPrice()))
            .toList();
    }

    private List<DiscountDto> mapDiscounts(Order order) {
        return order.getDiscounts().stream()
            .map(d -> new DiscountDto(d.description(), d.amount()))
            .toList();
    }
}
