package com.bistro.infrastructure.persistence;

import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.model.discount.Discount;
import com.bistro.domain.model.discount.DiscountStrategy;
import com.bistro.domain.repository.OrderRepository;
import com.bistro.domain.repository.ProductRepository;
import com.bistro.infrastructure.persistence.mapper.OrderJpaMapper;
import com.bistro.infrastructure.persistence.mapper.ProductJpaMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({OrderRepositoryAdapter.class, ProductRepositoryAdapter.class, OrderJpaMapper.class, ProductJpaMapper.class})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void save_and_findById_returnsOrderWithItemsAndDiscounts() {
        var pizza = productRepository.save(
            Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA));

        var discount = new Discount("Happy Hour (2for1)", new BigDecimal("10.00"));
        var strategy = mock(DiscountStrategy.class);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any())).thenReturn(discount);
        var order = Order.create(5, LocalDateTime.now(),
            List.of(new OrderItem(pizza, 2)),
            List.of(strategy));
        var saved = orderRepository.save(order);

        var found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getDiscounts()).hasSize(1);
        assertThat(found.getTotal()).isEqualByComparingTo("10.00");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        assertThat(orderRepository.findById(999L)).isEmpty();
    }

    @Test
    void findById_restoresStoredTotals_notRecalculated() {
        var pizza = productRepository.save(
            Product.create("Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA));

        var discount = new Discount("Test Discount", new BigDecimal("5.00"));
        var strategy = mock(DiscountStrategy.class);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any())).thenReturn(discount);
        var order = Order.create(7, LocalDateTime.now(),
            List.of(new OrderItem(pizza, 3)),
            List.of(strategy));
        var saved = orderRepository.save(order);

        var found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSubtotal()).isEqualByComparingTo("30.00");
        assertThat(found.getTotal()).isEqualByComparingTo("25.00");
        assertThat(found.getDiscounts()).hasSize(1);
        assertThat(found.getDiscounts().getFirst().description()).isEqualTo("Test Discount");
    }
}
