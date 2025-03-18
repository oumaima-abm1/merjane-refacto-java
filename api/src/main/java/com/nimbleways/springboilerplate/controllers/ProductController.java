package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class ProductController {

    private final ProductService productService;
    private final OrderRepository orderRepository;

    public ProductController(ProductService productService, OrderRepository orderRepository) {
        this.productService = productService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        Set<Product> products = order.getItems();
        products.forEach(this::processProduct);

        return new ProcessOrderResponse(order.getId());
    }

    public void processProduct(Product product) {
        switch (product.getType()) {
            case NORMAL -> productService.handleNormalProduct(product);
            case SEASONAL -> productService.handleSeasonalProduct(product);
            case EXPIRABLE -> productService.handleExpiredProduct(product);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getType());
        }
    }

}
