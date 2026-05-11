package com.bistro.infrastructure.web;

import com.bistro.infrastructure.web.dto.ProductDto;
import com.bistro.infrastructure.web.mapper.ProductMapper;
import com.bistro.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll() {
        return ResponseEntity.ok(productService.findAll().stream().map(productMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productMapper.toDto(productService.findById(id)));
    }
}
