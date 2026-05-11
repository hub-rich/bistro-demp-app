package com.bistro.infrastructure.web.mapper;

import com.bistro.infrastructure.web.dto.request.OrderItemRequest;
import com.bistro.infrastructure.web.dto.request.OrderRequest;
import com.bistro.application.service.command.OrderCommand;
import com.bistro.domain.model.discount.Discount;
import com.bistro.domain.model.discount.DiscountStrategy;
import com.bistro.domain.model.Order;
import com.bistro.domain.model.OrderItem;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toDto_mapsAllFields() {
        var pizza = Product.reconstitute(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);

        var discount = new Discount("Happy Hour (2for1)", new BigDecimal("10.00"));
        var strategy = mock(DiscountStrategy.class);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.apply(any(), any())).thenReturn(discount);
        var order = Order.create(10, LocalDateTime.of(2026, 5, 9, 18, 0),
            List.of(new OrderItem(pizza, 2)),
            List.of(strategy));

        var dto = mapper.toDto(order, "receipt text");

        assertThat(dto.tableNumber()).isEqualTo(10);
        assertThat(dto.subtotal()).isEqualByComparingTo("20.00");
        assertThat(dto.total()).isEqualByComparingTo("10.00");
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().getFirst().productName()).isEqualTo("Margherita");
        assertThat(dto.items().getFirst().quantity()).isEqualTo(2);
        assertThat(dto.items().getFirst().unitPrice()).isEqualByComparingTo("10.00");
        assertThat(dto.discounts()).hasSize(1);
        assertThat(dto.discounts().getFirst().description()).isEqualTo("Happy Hour (2for1)");
        assertThat(dto.receipt()).isEqualTo("receipt text");
    }

    @Test
    void toCommand_mapsTableNumberAndItems() {
        var request = new OrderRequest(5, List.of(
            new OrderItemRequest(1L, 2),
            new OrderItemRequest(3L, 1)
        ));

        OrderCommand command = mapper.toCommand(request);

        assertThat(command.tableNumber()).isEqualTo(5);
        assertThat(command.items()).hasSize(2);
        assertThat(command.items().get(0).productId()).isEqualTo(1L);
        assertThat(command.items().get(0).quantity()).isEqualTo(2);
        assertThat(command.items().get(1).productId()).isEqualTo(3L);
        assertThat(command.items().get(1).quantity()).isEqualTo(1);
    }
}
