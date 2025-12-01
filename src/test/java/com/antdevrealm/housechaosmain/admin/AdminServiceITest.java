package com.antdevrealm.housechaosmain.admin;

import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class AdminServiceITest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void addProduct_createsProductThroughProductService() {
        CreateCategoryRequestDTO categoryRequest = new CreateCategoryRequestDTO("Furniture");
        CategoryResponseDTO createdCategory = adminService.addCategory(categoryRequest);

        CreateProductRequestDTO productRequest = new CreateProductRequestDTO(
                "Wooden Chair",
                "Comfortable wooden chair for dining room",
                new BigDecimal("149.99"),
                5,
                "http://example.com/chair.jpg",
                createdCategory.id()
        );

        ProductResponseDTO productResponse = adminService.addProduct(productRequest);

        Optional<ProductEntity> savedProduct = productRepository.findByIdAndIsActiveIsTrue(productResponse.id());
        assertThat(savedProduct).isPresent();
        assertThat(savedProduct.get().getName()).isEqualTo("Wooden Chair");
        assertThat(savedProduct.get().getDescription()).isEqualTo("Comfortable wooden chair for dining room");
        assertThat(savedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(savedProduct.get().getQuantity()).isEqualTo(5);
        assertThat(savedProduct.get().getCategory().getId()).isEqualTo(createdCategory.id());
        assertThat(savedProduct.get().isActive()).isTrue();
        assertThat(savedProduct.get().isNewArrival()).isTrue();
    }
}
