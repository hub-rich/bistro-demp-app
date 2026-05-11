package com.bistro.application.service;

import com.bistro.application.exception.OrderNotFoundException;
import com.bistro.application.exception.OrderValidationException;
import com.bistro.application.service.command.OrderCommand;
import com.bistro.application.service.command.OrderItemCommand;
import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.model.discount.Discount;
import com.bistro.domain.model.discount.DiscountPhase;
import com.bistro.domain.model.discount.DiscountStrategy;
import com.bistro.domain.repository.OrderRepository;
import com.bistro.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 6, 15, 14, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(productRepository, orderRepository, List.of(), FIXED_CLOCK);

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Product pizza() {
        return Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
    }

    private Product cola() {
        return Product.create("Cola", new BigDecimal("2.50"), ProductCategory.DRINK);
    }

    @Test
    void createOrder_calculatesSubtotalCorrectly() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(pizza()));
        when(productRepository.findById(2L)).thenReturn(Optional.of(cola()));
        var command = new OrderCommand(10, List.of(
            new OrderItemCommand(1L, 2),
            new OrderItemCommand(2L, 2)));

        var order = orderService.createOrder(command);

        assertThat(order.getSubtotal()).isEqualByComparingTo("25.00");
        assertThat(order.getTotal()).isEqualByComparingTo("25.00");
    }

    @Test
    void createOrder_appliesDiscountsToTotal() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(pizza()));

        var strategy = mock(DiscountStrategy.class);
        when(strategy.phase()).thenReturn(DiscountPhase.ORDER_LEVEL);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any()))
            .thenReturn(new Discount("Happy Hour (2for1)", new BigDecimal("10.00")));

        var serviceWithStrategy = new OrderService(productRepository, orderRepository, List.of(strategy), FIXED_CLOCK);
        var command = new OrderCommand(1, List.of(new OrderItemCommand(1L, 2)));

        var order = serviceWithStrategy.createOrder(command);

        assertThat(order.getTotal()).isEqualByComparingTo("10.00");
        assertThat(order.getDiscounts()).hasSize(1);
        assertThat(order.getDiscounts().getFirst().description()).isEqualTo("Happy Hour (2for1)");
    }

    @Test
    void createOrder_stampsOrderWithCurrentTime() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(pizza()));
        var command = new OrderCommand(1, List.of(new OrderItemCommand(1L, 1)));

        var order = orderService.createOrder(command);

        assertThat(order.getOrderTime()).isEqualTo(FIXED_TIME);
    }

    @Test
    void createOrder_throwsOrderValidationException_whenTableNumberIsNull() {
        var command = new OrderCommand(null, List.of(new OrderItemCommand(1L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(command))
            .isInstanceOf(OrderValidationException.class);
    }

    @Test
    void createOrder_throwsOrderValidationException_whenItemsIsNull() {
        var command = new OrderCommand(1, null);

        assertThatThrownBy(() -> orderService.createOrder(command))
            .isInstanceOf(OrderValidationException.class);
    }

    @Test
    void createOrder_throwsOrderValidationException_whenItemsIsEmpty() {
        var command = new OrderCommand(1, List.of());

        assertThatThrownBy(() -> orderService.createOrder(command))
            .isInstanceOf(OrderValidationException.class);
    }

    @Test
    void createOrder_throwsOrderValidationException_whenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        var command = new OrderCommand(1, List.of(new OrderItemCommand(99L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(command))
            .isInstanceOf(OrderValidationException.class)
            .hasMessageContaining("99");
    }

    @Test
    void getOrderById_returnsOrder_whenFound() {
        var order = Order.create(1, FIXED_TIME, List.of(new OrderItem(pizza(), 1)), List.of());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThat(orderService.getOrderById(1L)).isSameAs(order);
    }

    @Test
    void getOrderById_throwsOrderNotFoundException_whenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
            .isInstanceOf(OrderNotFoundException.class);
    }
}
