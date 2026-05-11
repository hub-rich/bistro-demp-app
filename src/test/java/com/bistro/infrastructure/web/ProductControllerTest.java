package com.bistro.infrastructure.web;

import com.bistro.infrastructure.web.dto.ProductDto;
import com.bistro.infrastructure.web.mapper.ProductMapper;
import com.bistro.application.exception.ProductNotFoundException;
import com.bistro.application.service.ProductService;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    private Product pizza() {
        return Product.reconstitute(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA);
    }

    @Test
    void getProducts_returns200WithList() throws Exception {
        var p = pizza();
        when(productService.findAll()).thenReturn(List.of(p));
        when(productMapper.toDto(p)).thenReturn(
                new ProductDto(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Margherita"))
                .andExpect(jsonPath("$[0].category").value("PIZZA"));
    }

    @Test
    void getProductById_returns200_whenFound() throws Exception {
        var p = pizza();
        when(productService.findById(1L)).thenReturn(p);
        when(productMapper.toDto(p)).thenReturn(
                new ProductDto(1L, "Margherita", new BigDecimal("10.00"), ProductCategory.PIZZA));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Margherita"));
    }

    @Test
    void getProductById_returns404_whenNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }
}
