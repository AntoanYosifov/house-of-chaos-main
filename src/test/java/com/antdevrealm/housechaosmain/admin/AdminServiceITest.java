package com.antdevrealm.housechaosmain.admin;

import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class AdminServiceITest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addProduct_createsProductThroughProductService() {
        CategoryEntity category = CategoryEntity.builder()
                .name("chair")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        CreateProductRequestDTO productRequest = new CreateProductRequestDTO(
                "Test Chair",
                "Test description for chair",
                new BigDecimal("149.99"),
                5,
                "http://example.com/chair.jpg",
                savedCategory.getId()
        );

        ProductResponseDTO productResponse = adminService.addProduct(productRequest);

        Optional<ProductEntity> savedProduct = productRepository.findByIdAndIsActiveIsTrue(productResponse.id());
        assertThat(savedProduct).isPresent();
        assertThat(savedProduct.get().getName()).isEqualTo("Test Chair");
        assertThat(savedProduct.get().getDescription()).isEqualTo("Test description for chair");
        assertThat(savedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(savedProduct.get().getQuantity()).isEqualTo(5);
        assertThat(savedProduct.get().getCategory().getId()).isEqualTo(savedCategory.getId());
        assertThat(savedProduct.get().isActive()).isTrue();
        assertThat(savedProduct.get().isNewArrival()).isTrue();
    }

    @Test
    void updateProduct_updatesProductThroughProductService() {
        CategoryEntity category = CategoryEntity.builder()
                .name("lamp")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        ProductEntity product = ProductEntity.builder()
                .name("Test Lamp")
                .description("Test description for lamp")
                .price(new BigDecimal("299.99"))
                .quantity(2)
                .imageUrl("http://example.com/lamp.jpg")
                .category(savedCategory)
                .newArrival(true)
                .isActive(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ProductEntity savedProduct = productRepository.save(product);

        UpdateProductRequestDTO updateRequest = new UpdateProductRequestDTO(
                "Updated test description for lamp",
                new BigDecimal("399.99")
        );

        ProductResponseDTO updatedProduct = adminService.updateProduct(updateRequest, savedProduct.getId());

        Optional<ProductEntity> updatedProductEntity = productRepository.findByIdAndIsActiveIsTrue(updatedProduct.id());
        assertThat(updatedProductEntity).isPresent();
        assertThat(updatedProductEntity.get().getDescription()).isEqualTo("Updated test description for lamp");
        assertThat(updatedProductEntity.get().getPrice()).isEqualByComparingTo(new BigDecimal("399.99"));
        assertThat(updatedProductEntity.get().getName()).isEqualTo("Test Lamp");
    }

    @Test
    void deleteProduct_deletesProductThroughProductService() {
        CategoryEntity category = CategoryEntity.builder()
                .name("desk")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        ProductEntity product = ProductEntity.builder()
                .name("Test Desk")
                .description("Test description for desk")
                .price(new BigDecimal("199.99"))
                .quantity(3)
                .imageUrl("http://example.com/desk.jpg")
                .category(savedCategory)
                .newArrival(true)
                .isActive(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ProductEntity savedProduct = productRepository.save(product);

        adminService.deleteProduct(savedProduct.getId());

        Optional<ProductEntity> deletedProduct = productRepository.findByIdAndIsActiveIsTrue(savedProduct.getId());
        assertThat(deletedProduct).isEmpty();

        Optional<ProductEntity> softDeletedProduct = productRepository.findById(savedProduct.getId());
        assertThat(softDeletedProduct).isPresent();
        assertThat(softDeletedProduct.get().isActive()).isFalse();
    }

    @Test
    void addCategory_createsCategoryThroughCategoryService() {
        CreateCategoryRequestDTO categoryRequest = new CreateCategoryRequestDTO("table");

        CategoryResponseDTO categoryResponse = adminService.addCategory(categoryRequest);

        Optional<CategoryEntity> savedCategory = categoryRepository.findById(categoryResponse.id());
        assertThat(savedCategory).isPresent();
        assertThat(savedCategory.get().getName()).isEqualTo("table");
    }

    @Test
    void deleteCategory_happyPath() {
        CategoryEntity category = CategoryEntity.builder()
                .name("couch")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        adminService.deleteCategory(savedCategory.getId());

        Optional<CategoryEntity> deletedCategory = categoryRepository.findById(savedCategory.getId());
        assertThat(deletedCategory).isEmpty();
    }

    @Test
    void deleteCategory_unhappyPath() {
        CategoryEntity category = CategoryEntity.builder()
                .name("table")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        ProductEntity product = ProductEntity.builder()
                .name("Test Table")
                .description("Test description for table")
                .price(new BigDecimal("249.99"))
                .quantity(4)
                .imageUrl("http://example.com/table.jpg")
                .category(savedCategory)
                .newArrival(true)
                .isActive(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        productRepository.save(product);

        assertThrows(BusinessRuleException.class, () -> adminService.deleteCategory(savedCategory.getId()));

        Optional<CategoryEntity> categoryStillExists = categoryRepository.findById(savedCategory.getId());
        assertThat(categoryStillExists).isPresent();
    }

    @Test
    void getAllUsers_returnsAllUsersExceptCurrentUser() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        UserEntity user1 = UserEntity.builder()
                .email("user1@test.com")
                .password("encodedPassword1")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user1.getRoles().add(userRole);
        UserEntity savedUser1 = userRepository.save(user1);

        UserEntity user2 = UserEntity.builder()
                .email("user2@test.com")
                .password("encodedPassword2")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user2.getRoles().add(userRole);
        UserEntity savedUser2 = userRepository.save(user2);

        UserEntity user3 = UserEntity.builder()
                .email("user3@test.com")
                .password("encodedPassword3")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user3.getRoles().add(userRole);
        UserEntity savedUser3 = userRepository.save(user3);

        List<UserResponseDTO> result = adminService.getAllUsers(savedUser1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(UserResponseDTO::id)).containsExactlyInAnyOrder(savedUser2.getId(), savedUser3.getId());
        assertThat(result.stream().map(UserResponseDTO::id)).doesNotContain(savedUser1.getId());
    }
}
