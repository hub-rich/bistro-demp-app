package com.bistro.infrastructure.web;

import com.bistro.infrastructure.web.dto.OrderItemDto;
import com.bistro.infrastructure.web.dto.OrderDto;
import com.bistro.infrastructure.web.mapper.OrderMapper;
import com.bistro.application.exception.OrderNotFoundException;
import com.bistro.application.exception.OrderValidationException;
import com.bistro.application.service.OrderService;
import com.bistro.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OrderController.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private OrderMapper orderMapper;
    @MockitoBean
    private ReceiptFormatter receiptFormatter;

    private OrderDto sampleResponse() {
        return new OrderDto(1L, 10, LocalDateTime.now(),
                List.of(new OrderItemDto("Margherita", 2, new BigDecimal("10.00"))),
                new BigDecimal("20.00"), List.of(), new BigDecimal("20.00"), "receipt");
    }

    @Test
    void postOrder_returns201_onSuccess() throws Exception {
        when(orderService.createOrder(any())).thenReturn(mock(Order.class));
        when(receiptFormatter.generateReceipt(any())).thenReturn("receipt");
        when(orderMapper.toDto(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":10,"items":[{"productId":1,"quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value(10));
    }

    @Test
    void postOrder_returns422_whenTableNumberIsNull() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":null,"items":[{"productId":1,"quantity":1}]}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postOrder_returns422_whenItemsIsEmpty() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":1,"items":[]}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postOrder_returns422_whenQuantityIsZero() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":10,"items":[{"productId":1,"quantity":0}]}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postOrder_returns422_whenProductNotFound() throws Exception {
        when(orderService.createOrder(any())).thenThrow(new OrderValidationException("Product not found: 99"));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":10,"items":[{"productId":99,"quantity":1}]}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void postOrder_returns400_whenJsonMalformed() throws Exception {
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postOrder_returns500_onUnexpectedError() throws Exception {
        when(orderService.createOrder(any())).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tableNumber":10,"items":[{"productId":1,"quantity":1}]}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void getOrderById_returns200_whenFound() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(mock(Order.class));
        when(receiptFormatter.generateReceipt(any())).thenReturn("receipt");
        when(orderMapper.toDto(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrderById_returns404_whenNotFound() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound());
    }
}
