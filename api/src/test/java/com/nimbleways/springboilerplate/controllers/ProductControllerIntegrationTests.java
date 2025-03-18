package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nimbleways.springboilerplate.entities.ProductType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        orderRepository.deleteAll();

        List<Product> products = createProducts();
        productRepository.saveAll(products);

        testOrder = createOrder(products);
        orderRepository.save(testOrder);
    }

    @Test
    public void processOrder_ShouldReturnOkAndUpdateOrder() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", testOrder.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        Order resultOrder = orderRepository.findById(testOrder.getId()).orElse(null);
        assertThat(resultOrder).isNotNull();
        assertThat(resultOrder.getId()).isEqualTo(testOrder.getId());
    }

    private Order createOrder(List<Product> products) {
        Order order = new Order();
        order.setItems(Set.copyOf(products));
        return order;
    }

    private List<Product> createProducts() {
        return Stream.of(
                new Product(null, 15, 30, NORMAL, "USB Cable", null, null, null),
                new Product(null, 10, 0, NORMAL, "USB Dongle", null, null, null),
                new Product(null, 15, 30, EXPIRABLE, "Butter", LocalDate.now().plusDays(26), null, null),
                new Product(null, 90, 6, EXPIRABLE, "Milk", LocalDate.now().minusDays(2), null, null),
                new Product(null, 15, 30, SEASONAL, "Watermelon", null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58)),
                new Product(null, 15, 30, SEASONAL, "Grapes", null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240))
        ).collect(Collectors.toList());
    }
}
