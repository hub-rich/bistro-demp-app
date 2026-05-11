package com.bistro.domain.repository;

import com.bistro.domain.model.Order;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    Order save(Order order);
}
