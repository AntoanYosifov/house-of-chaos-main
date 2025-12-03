package com.antdevrealm.housechaosmain.product;

import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ImgUrlExpander imgUrlExpander;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenExistingProductId_whenGetById_thenProductResponseDTOIsReturned() {
        UUID productId = UUID.randomUUID();

        ProductEntity productEntity = ProductEntity.builder()
                .id(productId)
                .name("Test Chair")
                .description("Test description for chair")
                .price(new BigDecimal("149.99"))
                .quantity(5)
                .imageUrl("/images/chair.jpg")
                .isActive(true)
                .build();

        when(productRepository.findByIdAndIsActiveIsTrue(productId)).thenReturn(Optional.of(productEntity));
        when(imgUrlExpander.toPublicUrl("/images/chair.jpg")).thenReturn("http://localhost:8080/images/chair.jpg");

        ProductResponseDTO result = productService.getById(productId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo("Test Chair");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(result.quantity()).isEqualTo(5);

        verify(productRepository, times(1)).findByIdAndIsActiveIsTrue(productId);
    }

    @Test
    void givenNonExistentProductId_whenGetById_thenResourceNotFoundExceptionIsThrown() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findByIdAndIsActiveIsTrue(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getById(productId));

        verify(productRepository, times(1)).findByIdAndIsActiveIsTrue(productId);
    }
}
