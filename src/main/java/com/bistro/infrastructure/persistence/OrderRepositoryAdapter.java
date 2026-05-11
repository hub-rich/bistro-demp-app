package com.bistro.infrastructure.persistence;

import com.bistro.domain.model.Order;
import com.bistro.domain.repository.OrderRepository;
import com.bistro.infrastructure.persistence.mapper.OrderJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository orderRepository;
    private final OrderJpaMapper orderJpaMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id).map(orderJpaMapper::toDomain);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = orderJpaMapper.toEntity(order);
        var saved = orderRepository.save(entity);
        return orderJpaMapper.toDomain(saved);
    }
}
