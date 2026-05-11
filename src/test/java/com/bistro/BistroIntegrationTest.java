package com.bistro;

import com.bistro.infrastructure.web.dto.OrderDto;
import com.bistro.infrastructure.web.dto.ProductDto;
import com.bistro.infrastructure.web.dto.request.OrderItemRequest;
import com.bistro.infrastructure.web.dto.request.OrderRequest;
import com.bistro.domain.model.Product;
import com.bistro.domain.model.ProductCategory;
import com.bistro.domain.repository.ProductRepository;
import com.bistro.infrastructure.persistence.SpringDataOrderRepository;
import com.bistro.infrastructure.persistence.SpringDataProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class BistroIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SpringDataOrderRepository springDataOrderRepository;

    @Autowired
    private SpringDataProductRepository springDataProductRepository;

    private Product pizza;

    @BeforeEach
    void setUp() {
        springDataOrderRepository.deleteAll();
        springDataProductRepository.deleteAll();
        pizza = productRepository.save(Product.create("Margherita", new BigDecimal("12.00"), ProductCategory.PIZZA));
    }

    @Test
    void getAllProducts_returnsAllSeededProducts() {
        var response = restTemplate.getForEntity("/products", ProductDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].name()).isEqualTo("Margherita");
    }

    @Test
    void getProductById_returnsProduct() {
        var response = restTemplate.getForEntity("/products/" + pizza.getId(), ProductDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Margherita");
        assertThat(response.getBody().price()).isEqualByComparingTo("12.00");
        assertThat(response.getBody().category()).isEqualTo(ProductCategory.PIZZA);
    }

    @Test
    void getProductById_whenNotFound_returns404() {
        var response = restTemplate.getForEntity("/products/999", ProductDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createOrder_withSmallOrder_returnsCreatedWithCorrectTotal() {
        var request = new OrderRequest(5, List.of(new OrderItemRequest(pizza.getId(), 2)));

        var response = restTemplate.postForEntity("/orders", request, OrderDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getBody().subtotal()).isEqualByComparingTo("24.00");
        assertThat(response.getBody().total()).isEqualByComparingTo("24.00");
        assertThat(response.getBody().discounts()).isEmpty();
    }

    @Test
    void createOrder_withLargeOrder_appliesLargeOrderDiscount() {
        var request = new OrderRequest(5, List.of(new OrderItemRequest(pizza.getId(), 4)));

        var response = restTemplate.postForEntity("/orders", request, OrderDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().subtotal()).isEqualByComparingTo("48.00");
        assertThat(response.getBody().discounts()).hasSize(1);
        assertThat(response.getBody().total()).isEqualByComparingTo("43.20");
    }

    @Test
    void getOrderById_returnsOrder() {
        var request = new OrderRequest(7, List.of(new OrderItemRequest(pizza.getId(), 1)));
        var created = restTemplate.postForEntity("/orders", request, OrderDto.class);
        var orderId = created.getBody().id();

        var response = restTemplate.getForEntity("/orders/" + orderId, OrderDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().tableNumber()).isEqualTo(7);
        assertThat(response.getBody().total()).isEqualByComparingTo("12.00");
    }

    @Test
    void getOrderById_whenNotFound_returns404() {
        var response = restTemplate.getForEntity("/orders/999", OrderDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createOrder_withNullTableNumber_returns422() {
        var request = new OrderRequest(null, List.of(new OrderItemRequest(pizza.getId(), 1)));

        var response = restTemplate.postForEntity("/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void createOrder_withUnknownProductId_returns422() {
        var request = new OrderRequest(5, List.of(new OrderItemRequest(999L, 1)));

        var response = restTemplate.postForEntity("/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void createOrder_withEmptyItemsList_returns422() {
        var request = new OrderRequest(5, List.of());

        var response = restTemplate.postForEntity("/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
