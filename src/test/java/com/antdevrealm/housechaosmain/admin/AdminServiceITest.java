package com.antdevrealm.housechaosmain.admin;

import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AdminServiceITest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void addProduct_createsProductThroughProductService() {
        CreateCategoryRequestDTO categoryRequest = new CreateCategoryRequestDTO("chair");
        CategoryResponseDTO createdCategory = adminService.addCategory(categoryRequest);

        CreateProductRequestDTO productRequest = new CreateProductRequestDTO(
                "Test Chair",
                "Test description for chair",
                new BigDecimal("149.99"),
                5,
                "http://example.com/chair.jpg",
                createdCategory.id()
        );

        ProductResponseDTO productResponse = adminService.addProduct(productRequest);

        Optional<ProductEntity> savedProduct = productRepository.findByIdAndIsActiveIsTrue(productResponse.id());
        assertThat(savedProduct).isPresent();
        assertThat(savedProduct.get().getName()).isEqualTo("Test Chair");
        assertThat(savedProduct.get().getDescription()).isEqualTo("Test description for chair");
        assertThat(savedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(savedProduct.get().getQuantity()).isEqualTo(5);
        assertThat(savedProduct.get().getCategory().getId()).isEqualTo(createdCategory.id());
        assertThat(savedProduct.get().isActive()).isTrue();
        assertThat(savedProduct.get().isNewArrival()).isTrue();
    }

    @Test
    void updateProduct_updatesProductThroughProductService() {
        CreateCategoryRequestDTO categoryRequest = new CreateCategoryRequestDTO("lamp");
        CategoryResponseDTO createdCategory = adminService.addCategory(categoryRequest);

        CreateProductRequestDTO createProductRequest = new CreateProductRequestDTO(
                "Test Lamp",
                "Test description for lamp",
                new BigDecimal("299.99"),
                2,
                "http://example.com/lamp.jpg",
                createdCategory.id()
        );

        ProductResponseDTO createdProduct = adminService.addProduct(createProductRequest);

        UpdateProductRequestDTO updateRequest = new UpdateProductRequestDTO(
                "Updated test description for lamp",
                new BigDecimal("399.99")
        );

        ProductResponseDTO updatedProduct = adminService.updateProduct(updateRequest, createdProduct.id());

        Optional<ProductEntity> savedProduct = productRepository.findByIdAndIsActiveIsTrue(updatedProduct.id());
        assertThat(savedProduct).isPresent();
        assertThat(savedProduct.get().getDescription()).isEqualTo("Updated test description for lamp");
        assertThat(savedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("399.99"));
        assertThat(savedProduct.get().getName()).isEqualTo("Test Lamp");
    }
}
