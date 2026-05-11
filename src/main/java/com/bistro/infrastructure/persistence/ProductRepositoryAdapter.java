package com.bistro.infrastructure.persistence;

import com.bistro.domain.model.Product;
import com.bistro.domain.repository.ProductRepository;
import com.bistro.infrastructure.persistence.mapper.ProductJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository productRepository;
    private final ProductJpaMapper productJpaMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id).map(productJpaMapper::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll().stream()
            .map(productJpaMapper::fromEntity)
            .toList();
    }

    @Override
    @Transactional
    public Product save(Product product) {
        var entity = productJpaMapper.toEntity(product);
        var saved = productRepository.save(entity);
        return productJpaMapper.fromEntity(saved);
    }
}
