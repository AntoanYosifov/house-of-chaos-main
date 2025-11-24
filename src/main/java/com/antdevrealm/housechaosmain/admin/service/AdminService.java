package com.antdevrealm.housechaosmain.admin.service;

import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public AdminService(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    public ProductResponseDTO addProduct(CreateProductRequestDTO dto) {
        return this.productService.create(dto);
    }

    public ProductResponseDTO updateProduct(UpdateProductRequestDTO dto, UUID id) {
        return this.productService.update(dto, id);
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
