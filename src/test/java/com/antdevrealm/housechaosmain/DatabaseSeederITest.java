package com.antdevrealm.housechaosmain;

import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("database-seeder-test")
public class DatabaseSeederITest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @Test
    void run_seedsRoles() throws IOException {
        when(cloudinaryService.uploadImage(any(byte[].class), anyString(), anyString()))
                .thenReturn("house-of-chaos/mock-image-id");

        assertThat(roleRepository.count()).isEqualTo(2);
        assertThat(roleRepository.findByRole(UserRole.USER)).isPresent();
        assertThat(roleRepository.findByRole(UserRole.ADMIN)).isPresent();
    }

    @Test
    void run_seedsDefaultAdminUser() throws IOException {
        when(cloudinaryService.uploadImage(any(byte[].class), anyString(), anyString()))
                .thenReturn("house-of-chaos/mock-image-id");

        assertThat(userRepository.count()).isEqualTo(1);
        var adminUser = userRepository.findByEmail("admin@email.com");
        assertThat(adminUser).isPresent();
        assertThat(adminUser.get().getRoles()).hasSize(2);
        assertThat(adminUser.get().getRoles().stream()
                .anyMatch(role -> role.getRole() == UserRole.USER)).isTrue();
        assertThat(adminUser.get().getRoles().stream()
                .anyMatch(role -> role.getRole() == UserRole.ADMIN)).isTrue();


        assertThat(cartRepository.count()).isEqualTo(1);
        var adminCart = cartRepository.findByOwnerId(adminUser.get().getId());
        assertThat(adminCart).isPresent();
    }

    @Test
    void run_seedsCategories() throws IOException {
        when(cloudinaryService.uploadImage(any(byte[].class), anyString(), anyString()))
                .thenReturn("house-of-chaos/mock-image-id");

        assertThat(categoryRepository.count()).isEqualTo(4);
        assertThat(categoryRepository.findByName("chair")).isPresent();
        assertThat(categoryRepository.findByName("table")).isPresent();
        assertThat(categoryRepository.findByName("couch")).isPresent();
        assertThat(categoryRepository.findByName("lamp")).isPresent();
    }

    @Test
    void run_seedsProducts() throws IOException {
        when(cloudinaryService.uploadImage(any(byte[].class), anyString(), anyString()))
                .thenReturn("house-of-chaos/mock-image-id");

        assertThat(productRepository.count()).isEqualTo(40);

        var chairCategory = categoryRepository.findByName("chair").orElseThrow();
        var tableCategory = categoryRepository.findByName("table").orElseThrow();
        var couchCategory = categoryRepository.findByName("couch").orElseThrow();
        var lampCategory = categoryRepository.findByName("lamp").orElseThrow();

        assertThat(productRepository.findAllByCategoryAndIsActiveIsTrue(chairCategory)).hasSize(10);
        assertThat(productRepository.findAllByCategoryAndIsActiveIsTrue(tableCategory)).hasSize(10);
        assertThat(productRepository.findAllByCategoryAndIsActiveIsTrue(couchCategory)).hasSize(10);
        assertThat(productRepository.findAllByCategoryAndIsActiveIsTrue(lampCategory)).hasSize(10);
    }

    @Test
    void run_whenRolesExist_doesNotDuplicateRoles() throws IOException {
        when(cloudinaryService.uploadImage(any(byte[].class), anyString(), anyString()))
                .thenReturn("house-of-chaos/mock-image-id");

        long initialRoleCount = roleRepository.count();
        assertThat(initialRoleCount).isEqualTo(2);

        assertThat(roleRepository.count()).isEqualTo(2);
    }

    @Test
    void run_whenDataExists_doesNotDuplicateData() throws IOException {
        when(cloudinaryService.uploadImage(any(byte[].class), anyString(), anyString()))
                .thenReturn("house-of-chaos/mock-image-id");

        long initialUserCount = userRepository.count();
        long initialCategoryCount = categoryRepository.count();
        long initialProductCount = productRepository.count();

        assertThat(initialUserCount).isEqualTo(1);
        assertThat(initialCategoryCount).isEqualTo(4);
        assertThat(initialProductCount).isEqualTo(40);

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(categoryRepository.count()).isEqualTo(4);
        assertThat(productRepository.count()).isEqualTo(40);
    }
}

