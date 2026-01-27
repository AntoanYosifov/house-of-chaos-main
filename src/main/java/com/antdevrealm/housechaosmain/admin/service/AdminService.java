package com.antdevrealm.housechaosmain.admin.service;

import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.product.dto.CreateProductForm;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {
    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public AdminService(ProductService productService, UserService userService, CategoryService categoryService) {
        this.productService = productService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public ProductResponseDTO addProduct(CreateProductForm form, MultipartFile file) throws IOException {
        return this.productService.create(form, file);
    }

    public ProductResponseDTO updateProduct(UpdateProductRequestDTO dto, UUID id) {
        return this.productService.update(dto, id);
    }

    public void deleteProduct(UUID id) {
        this.productService.softDelete(id);
    }

    public CategoryResponseDTO addCategory(CreateCategoryRequestDTO dto) {
        return this.categoryService.create(dto);
    }

    public void deleteCategory(UUID id) {
        CategoryEntity categoryEntity = this.categoryService.getById(id);

        boolean productExistsByCategory = this.productService.existsByCategory(categoryEntity);

        if(productExistsByCategory) {
            throw new BusinessRuleException(String.format("Can not delete category with ID: %s because it has products", id));
        }
        this.categoryService.delete(categoryEntity);
    }

    public List<UserResponseDTO> getAllUsers(UUID userId) {
        return this.userService.getAll().stream().filter(u -> !u.id().equals(userId)).toList();
    }

    public UserResponseDTO promoteToAdmin(UUID userId) {
        return this.userService.addAdminRole(userId);
    }

    public UserResponseDTO demoteFromAdmin(UUID id) {
        return this.userService.removeAdminRole(id);
    }

}
