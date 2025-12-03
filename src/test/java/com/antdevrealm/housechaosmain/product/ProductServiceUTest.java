package com.antdevrealm.housechaosmain.product;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
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
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void givenCategoryWithProducts_whenGetAllByCategoryId_thenListOfProductsIsReturned() {
        UUID categoryId = UUID.randomUUID();

        CategoryEntity category = CategoryEntity.builder()
                .id(categoryId)
                .name("chair")
                .build();

        ProductEntity product1 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Chair 1")
                .description("Test description for chair 1")
                .price(new BigDecimal("149.99"))
                .quantity(5)
                .imageUrl("/images/chair1.jpg")
                .isActive(true)
                .build();

        ProductEntity product2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Chair 2")
                .description("Test description for chair 2")
                .price(new BigDecimal("199.99"))
                .quantity(3)
                .imageUrl("/images/chair2.jpg")
                .isActive(true)
                .build();

        when(categoryService.getById(categoryId)).thenReturn(category);
        when(productRepository.findAllByCategoryAndIsActiveIsTrue(category)).thenReturn(List.of(product1, product2));
        when(imgUrlExpander.toPublicUrl("/images/chair1.jpg")).thenReturn("http://localhost:8080/images/chair1.jpg");
        when(imgUrlExpander.toPublicUrl("/images/chair2.jpg")).thenReturn("http://localhost:8080/images/chair2.jpg");

        List<ProductResponseDTO> result = productService.getAllByCategoryId(categoryId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Test Chair 1");
        assertThat(result.get(1).name()).isEqualTo("Test Chair 2");

        verify(categoryService, times(1)).getById(categoryId);
        verify(productRepository, times(1)).findAllByCategoryAndIsActiveIsTrue(category);
    }

    @Test
    void givenCategoryWithNoProducts_whenGetAllByCategoryId_thenEmptyListIsReturned() {
        UUID categoryId = UUID.randomUUID();

        CategoryEntity category = CategoryEntity.builder()
                .id(categoryId)
                .name("lamp")
                .build();

        when(categoryService.getById(categoryId)).thenReturn(category);
        when(productRepository.findAllByCategoryAndIsActiveIsTrue(category)).thenReturn(new ArrayList<>());

        List<ProductResponseDTO> result = productService.getAllByCategoryId(categoryId);

        assertThat(result).isEmpty();

        verify(categoryService, times(1)).getById(categoryId);
        verify(productRepository, times(1)).findAllByCategoryAndIsActiveIsTrue(category);
    }
}
