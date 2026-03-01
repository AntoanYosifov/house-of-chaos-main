package com.antdevrealm.housechaosmain.product;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenExistingProductId_whenGetById_thenProductResponseDTOIsReturned() {
        UUID productId = UUID.randomUUID();

        String mockPublicId = "house-of-chaos/chair/test-chair-id";
        String mockThumbUrl = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-chair-id";
        String mockLargeUrl = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-chair-id";
        ProductEntity productEntity = ProductEntity.builder()
                .id(productId)
                .name("Test Chair")
                .description("Test description for chair")
                .price(new BigDecimal("149.99"))
                .quantity(5)
                .imagePublicId(mockPublicId)
                .isActive(true)
                .build();

        when(productRepository.findByIdAndIsActiveIsTrue(productId)).thenReturn(Optional.of(productEntity));
        when(cloudinaryService.buildThumbUrl(mockPublicId)).thenReturn(mockThumbUrl);
        when(cloudinaryService.buildLargeUrl(mockPublicId)).thenReturn(mockLargeUrl);

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
    void givenCategoryWithProducts_whenGetAll_thenPageOfProductsIsReturned() {
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();

        CategoryEntity category = CategoryEntity.builder()
                .id(categoryId)
                .name("chair")
                .build();

        String mockPublicId1 = "house-of-chaos/chair/test-chair-1-id";
        String mockThumbUrl1 = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-chair-1-id";
        String mockLargeUrl1 = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-chair-1-id";
        ProductEntity product1 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Chair 1")
                .description("Test description for chair 1")
                .price(new BigDecimal("149.99"))
                .quantity(5)
                .imagePublicId(mockPublicId1)
                .isActive(true)
                .build();

        String mockPublicId2 = "house-of-chaos/chair/test-chair-2-id";
        String mockThumbUrl2 = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-chair-2-id";
        String mockLargeUrl2 = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-chair-2-id";
        ProductEntity product2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Chair 2")
                .description("Test description for chair 2")
                .price(new BigDecimal("199.99"))
                .quantity(3)
                .imagePublicId(mockPublicId2)
                .isActive(true)
                .build();

        when(categoryService.getById(categoryId)).thenReturn(category);
        when(productRepository.findAllByCategoryAndIsActiveIsTrue(category, pageable))
                .thenReturn(new PageImpl<>(List.of(product1, product2)));
        when(cloudinaryService.buildThumbUrl(mockPublicId1)).thenReturn(mockThumbUrl1);
        when(cloudinaryService.buildLargeUrl(mockPublicId1)).thenReturn(mockLargeUrl1);
        when(cloudinaryService.buildThumbUrl(mockPublicId2)).thenReturn(mockThumbUrl2);
        when(cloudinaryService.buildLargeUrl(mockPublicId2)).thenReturn(mockLargeUrl2);

        Page<ProductResponseDTO> result = productService.getAll(categoryId, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Chair 1");
        assertThat(result.getContent().get(1).name()).isEqualTo("Test Chair 2");

        verify(categoryService, times(1)).getById(categoryId);
        verify(productRepository, times(1)).findAllByCategoryAndIsActiveIsTrue(category, pageable);
    }

    @Test
    void givenCategoryWithNoProducts_whenGetAll_thenEmptyPageIsReturned() {
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();

        CategoryEntity category = CategoryEntity.builder()
                .id(categoryId)
                .name("lamp")
                .build();

        when(categoryService.getById(categoryId)).thenReturn(category);
        when(productRepository.findAllByCategoryAndIsActiveIsTrue(category, pageable))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<ProductResponseDTO> result = productService.getAll(categoryId, pageable);

        assertThat(result.getContent()).isEmpty();

        verify(categoryService, times(1)).getById(categoryId);
        verify(productRepository, times(1)).findAllByCategoryAndIsActiveIsTrue(category, pageable);
    }

    @Test
    void givenNewArrivalsExist_whenGetNewArrivals_thenPageOfNewArrivalsIsReturned() {
        Pageable pageable = Pageable.unpaged();

        String mockPublicId1 = "house-of-chaos/chair/test-chair-id";
        String mockThumbUrl1 = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-chair-id";
        String mockLargeUrl1 = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-chair-id";
        ProductEntity product1 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("New Chair")
                .description("Test description for new chair")
                .price(new BigDecimal("149.99"))
                .quantity(5)
                .imagePublicId(mockPublicId1)
                .newArrival(true)
                .isActive(true)
                .build();

        String mockPublicId2 = "house-of-chaos/lamp/test-lamp-id";
        String mockThumbUrl2 = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-lamp-id";
        String mockLargeUrl2 = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-lamp-id";
        ProductEntity product2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("New Lamp")
                .description("Test description for new lamp")
                .price(new BigDecimal("99.99"))
                .quantity(3)
                .imagePublicId(mockPublicId2)
                .newArrival(true)
                .isActive(true)
                .build();

        when(productRepository.findAllByNewArrivalIsTrueAndIsActiveIsTrueOrderByCreatedOnDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(product1, product2)));
        when(cloudinaryService.buildThumbUrl(mockPublicId1)).thenReturn(mockThumbUrl1);
        when(cloudinaryService.buildLargeUrl(mockPublicId1)).thenReturn(mockLargeUrl1);
        when(cloudinaryService.buildThumbUrl(mockPublicId2)).thenReturn(mockThumbUrl2);
        when(cloudinaryService.buildLargeUrl(mockPublicId2)).thenReturn(mockLargeUrl2);

        Page<ProductResponseDTO> result = productService.getNewArrivals(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("New Chair");
        assertThat(result.getContent().get(1).name()).isEqualTo("New Lamp");

        verify(productRepository, times(1)).findAllByNewArrivalIsTrueAndIsActiveIsTrueOrderByCreatedOnDesc(pageable);
    }

    @Test
    void givenNoNewArrivals_whenGetNewArrivals_thenEmptyPageIsReturned() {
        Pageable pageable = Pageable.unpaged();

        when(productRepository.findAllByNewArrivalIsTrueAndIsActiveIsTrueOrderByCreatedOnDesc(pageable))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<ProductResponseDTO> result = productService.getNewArrivals(pageable);

        assertThat(result.getContent()).isEmpty();

        verify(productRepository, times(1)).findAllByNewArrivalIsTrueAndIsActiveIsTrueOrderByCreatedOnDesc(pageable);
    }

    @Test
    void givenCheapestProductsExist_whenGetCheapest_thenPageOfCheapestIsReturned() {
        Pageable pageable = Pageable.unpaged();

        String mockPublicId1 = "house-of-chaos/table/test-table-id";
        String mockThumbUrl1 = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-table-id";
        String mockLargeUrl1 = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-table-id";
        ProductEntity product1 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Cheap Table")
                .description("Test description for cheap table")
                .price(new BigDecimal("49.99"))
                .quantity(10)
                .imagePublicId(mockPublicId1)
                .isActive(true)
                .build();

        String mockPublicId2 = "house-of-chaos/desk/test-desk-id";
        String mockThumbUrl2 = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-desk-id";
        String mockLargeUrl2 = "https://res.cloudinary.com/test/image/upload/w_1200,c_limit/test-desk-id";
        ProductEntity product2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Cheap Desk")
                .description("Test description for cheap desk")
                .price(new BigDecimal("79.99"))
                .quantity(7)
                .imagePublicId(mockPublicId2)
                .isActive(true)
                .build();

        when(productRepository.findAllByIsActiveIsTrueOrderByPriceAsc(pageable))
                .thenReturn(new PageImpl<>(List.of(product1, product2)));
        when(cloudinaryService.buildThumbUrl(mockPublicId1)).thenReturn(mockThumbUrl1);
        when(cloudinaryService.buildLargeUrl(mockPublicId1)).thenReturn(mockLargeUrl1);
        when(cloudinaryService.buildThumbUrl(mockPublicId2)).thenReturn(mockThumbUrl2);
        when(cloudinaryService.buildLargeUrl(mockPublicId2)).thenReturn(mockLargeUrl2);

        Page<ProductResponseDTO> result = productService.getCheapest(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("Cheap Table");
        assertThat(result.getContent().get(1).name()).isEqualTo("Cheap Desk");

        verify(productRepository, times(1)).findAllByIsActiveIsTrueOrderByPriceAsc(pageable);
    }

    @Test
    void givenNoCheapestProducts_whenGetCheapest_thenEmptyPageIsReturned() {
        Pageable pageable = Pageable.unpaged();

        when(productRepository.findAllByIsActiveIsTrueOrderByPriceAsc(pageable))
                .thenReturn(new PageImpl<>(new ArrayList<>()));

        Page<ProductResponseDTO> result = productService.getCheapest(pageable);

        assertThat(result.getContent()).isEmpty();

        verify(productRepository, times(1)).findAllByIsActiveIsTrueOrderByPriceAsc(pageable);
    }
}
