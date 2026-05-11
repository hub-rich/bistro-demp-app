package com.bistro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer tableNumber;

    @Column(nullable = false)
    private LocalDateTime orderTime;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDiscountJpaEntity> discounts;

    protected OrderJpaEntity() {}

    public OrderJpaEntity(Integer tableNumber, LocalDateTime orderTime, BigDecimal subtotal, BigDecimal total,
                          List<OrderItemJpaEntity> items, List<OrderDiscountJpaEntity> discounts) {
        this.tableNumber = tableNumber;
        this.orderTime = orderTime;
        this.subtotal = subtotal;
        this.total = total;
        this.items = items;
        this.discounts = discounts;
        items.forEach(item -> item.setOrder(this));
        discounts.forEach(discount -> discount.setOrder(this));
    }
}
