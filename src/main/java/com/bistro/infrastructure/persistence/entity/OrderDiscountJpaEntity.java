package com.bistro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_discounts")
@Getter
public class OrderDiscountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Setter
    private OrderJpaEntity order;

    protected OrderDiscountJpaEntity() {
    }

    public OrderDiscountJpaEntity(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }
}
