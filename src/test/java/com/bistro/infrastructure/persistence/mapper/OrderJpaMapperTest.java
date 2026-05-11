package com.bistro.infrastructure.persistence.mapper;

import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.model.discount.Discount;
import com.bistro.domain.model.discount.DiscountStrategy;
import com.bistro.infrastructure.persistence.SpringDataProductRepository;
import com.bistro.infrastructure.persistence.entity.OrderDiscountJpaEntity;
import com.bistro.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.bistro.infrastructure.persistence.entity.OrderJpaEntity;
import com.bistro.infrastructure.persistence.entity.ProductJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderJpaMapperTest {

    private static final LocalDateTime ORDER_TIME = LocalDateTime.of(2024, 6, 15, 12, 0);

    @Mock
    private SpringDataProductRepository productRepository;

    private OrderJpaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderJpaMapper(productRepository, new ProductJpaMapper());
    }

    private Product domainPizza() {
        return Product.reconstitute(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
    }

    private ProductJpaEntity entityPizza() {
        var entity = new ProductJpaEntity("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
        ReflectionTestUtils.setField(entity, "id", 1L);
        return entity;
    }

    @Test
    void toEntity_mapsOrderHeaderFields() {
        var order = Order.create(5, ORDER_TIME, List.of(), List.of());

        var entity = mapper.toEntity(order);

        assertThat(entity.getTableNumber()).isEqualTo(5);
        assertThat(entity.getOrderTime()).isEqualTo(ORDER_TIME);
        assertThat(entity.getSubtotal()).isEqualByComparingTo("0.00");
        assertThat(entity.getTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void toEntity_mapsItemsAndDiscounts() {
        when(productRepository.getReferenceById(1L)).thenReturn(entityPizza());

        var discount = new Discount("Happy Hour (2for1)", new BigDecimal("10.00"));
        var strategy = mock(DiscountStrategy.class);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any())).thenReturn(discount);
        var order = Order.create(3, ORDER_TIME,
            List.of(new OrderItem(domainPizza(), 2)),
            List.of(strategy));

        var entity = mapper.toEntity(order);

        assertThat(entity.getItems()).hasSize(1);
        assertThat(entity.getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(entity.getItems().getFirst().getProduct().getName()).isEqualTo("Margherita");
        assertThat(entity.getDiscounts()).hasSize(1);
        assertThat(entity.getDiscounts().getFirst().getDescription()).isEqualTo("Happy Hour (2for1)");
        assertThat(entity.getDiscounts().getFirst().getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void toDomain_mapsOrderHeaderFields() {
        var entity = new OrderJpaEntity(7, ORDER_TIME, new BigDecimal("25.00"), new BigDecimal("25.00"), List.of(), List.of());
        ReflectionTestUtils.setField(entity, "id", 99L);

        var order = mapper.toDomain(entity);

        assertThat(order.getId()).isEqualTo(99L);
        assertThat(order.getTableNumber()).isEqualTo(7);
        assertThat(order.getOrderTime()).isEqualTo(ORDER_TIME);
        assertThat(order.getSubtotal()).isEqualByComparingTo("25.00");
        assertThat(order.getTotal()).isEqualByComparingTo("25.00");
    }

    @Test
    void toDomain_mapsItemsWithProducts() {
        var itemEntity = new OrderItemJpaEntity(entityPizza(), 2);
        var orderEntity = new OrderJpaEntity(2, ORDER_TIME, new BigDecimal("20.00"), new BigDecimal("20.00"), List.of(itemEntity), List.of());

        var order = mapper.toDomain(orderEntity);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().product().getName()).isEqualTo("Margherita");
        assertThat(order.getItems().getFirst().quantity()).isEqualTo(2);
    }

    @Test
    void toDomain_mapsDiscount() {
        var discountEntity = new OrderDiscountJpaEntity("Happy Hour (2for1)", new BigDecimal("10.00"));
        var orderEntity = new OrderJpaEntity(4, ORDER_TIME, new BigDecimal("20.00"), new BigDecimal("10.00"), List.of(), List.of(discountEntity));

        var order = mapper.toDomain(orderEntity);

        assertThat(order.getDiscounts()).hasSize(1);
        assertThat(order.getDiscounts().getFirst().description()).isEqualTo("Happy Hour (2for1)");
        assertThat(order.getDiscounts().getFirst().amount()).isEqualByComparingTo("10.00");
    }
}
