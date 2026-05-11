package com.bistro.infrastructure.web;

import com.bistro.infrastructure.web.dto.OrderDto;
import com.bistro.infrastructure.web.dto.request.OrderRequest;
import com.bistro.infrastructure.web.mapper.OrderMapper;
import com.bistro.application.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ReceiptFormatter receiptFormatter;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest request) {
        var order = orderService.createOrder(orderMapper.toCommand(request));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(order.getId())
            .toUri();
        return ResponseEntity.created(location)
            .body(orderMapper.toDto(order, receiptFormatter.generateReceipt(order)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        var order = orderService.getOrderById(id);
        return ResponseEntity.ok(orderMapper.toDto(order, receiptFormatter.generateReceipt(order)));
    }
}
