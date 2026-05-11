package com.bistro.infrastructure.persistence.mapper;

import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.discount.Discount;
import com.bistro.infrastructure.persistence.SpringDataProductRepository;
import com.bistro.infrastructure.persistence.entity.OrderDiscountJpaEntity;
import com.bistro.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.bistro.infrastructure.persistence.entity.OrderJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderJpaMapper {

    private final SpringDataProductRepository productRepository;
    private final ProductJpaMapper productJpaMapper;

    public OrderJpaEntity toEntity(Order order) {
        var itemEntities = order.getItems().stream()
            .map(this::toItemEntity)
            .toList();

        var discountEntities = order.getDiscounts().stream()
            .map(d -> new OrderDiscountJpaEntity(d.description(), d.amount()))
            .toList();

        return new OrderJpaEntity(order.getTableNumber(), order.getOrderTime(), order.getSubtotal(), order.getTotal(),
            itemEntities, discountEntities);
    }

    private OrderItemJpaEntity toItemEntity(OrderItem item) {
        var productEntity = productRepository.getReferenceById(item.product().getId());
        return new OrderItemJpaEntity(productEntity, item.quantity());
    }

    public Order toDomain(OrderJpaEntity entity) {
        var items = entity.getItems().stream()
            .map(itemEntity -> {
                var product = productJpaMapper.fromEntity(itemEntity.getProduct());
                return new OrderItem(product, itemEntity.getQuantity());
            })
            .toList();

        var discounts = entity.getDiscounts().stream()
            .map(d -> new Discount(d.getDescription(), d.getAmount()))
            .toList();

        return Order.reconstitute(entity.getId(), entity.getTableNumber(), entity.getOrderTime(), entity.getSubtotal(),
            entity.getTotal(), items, discounts);
    }
}
