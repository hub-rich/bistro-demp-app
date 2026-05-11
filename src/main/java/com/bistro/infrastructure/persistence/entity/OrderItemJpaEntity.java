package com.bistro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ProductJpaEntity product;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Setter
    private OrderJpaEntity order;

    protected OrderItemJpaEntity() {}

    public OrderItemJpaEntity(ProductJpaEntity product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}
