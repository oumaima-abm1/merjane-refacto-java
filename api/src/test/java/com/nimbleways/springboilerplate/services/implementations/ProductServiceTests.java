package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.nimbleways.springboilerplate.entities.ProductType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;


    @Test
    public void handleNormalProduct_ShouldDecrementStock_WhenAvailable() {
        Product product = new Product(null, 0, 10, NORMAL, "USB Cable", null, null, null); // FIXED ORDER
        when(productRepository.save(product)).thenReturn(product);

        productService.handleNormalProduct(product);

        assertThat(product.getAvailable()).isEqualTo(9);
        verify(productRepository, times(1)).save(product);
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }
    @Test
    public void handleNormalProduct_ShouldNotifyDelay_WhenOutOfStock() {
        Product product = new Product(null, 5, 0, NORMAL, "USB Cable", null, null, null);

        when(productRepository.save(product)).thenReturn(product);

        productService.handleNormalProduct(product);

        verify(notificationService, times(1)).sendDelayNotification(eq(5), eq("USB Cable"));
        verify(productRepository, times(1)).save(product);
        assertThat(product.getLeadTime()).isEqualTo(5);
    }

    @Test
    public void handleSeasonalProduct_ShouldNotifyOutOfStock_WhenSeasonOver() {
        Product product = new Product(null, 7, 0, SEASONAL, "Watermelon", null,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(5));

        productService.handleSeasonalProduct(product);

        verify(notificationService, times(1)).sendOutOfStockNotification(eq("Watermelon"));
        assertThat(product.getAvailable()).isEqualTo(0);
        verify(productRepository, times(1)).save(product);
    }



    @Test
    public void handleExpiredProduct_ShouldReduceStock_WhenNotExpired() {
        Product product = new Product(null, 0, 5, EXPIRABLE, "Milk", LocalDate.now().plusDays(3), null, null); // FIXED ORDER
        when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isEqualTo(4);
        verify(productRepository, times(1)).save(product);
        verify(notificationService, never()).sendExpirationNotification(anyString(), any());
    }

    @Test
    public void handleExpiredProduct_ShouldNotifyExpiration_WhenExpired() {
        Product product = new Product(null, 0, 0, EXPIRABLE, "Milk", LocalDate.now().minusDays(1), null, null); // FIXED ORDER

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isEqualTo(0);
        verify(notificationService, times(1)).sendExpirationNotification("Milk", product.getExpiryDate());
        verify(productRepository, times(1)).save(product);
    }
}
