package com.antdevrealm.housechaosmain.admin.web;


import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody @Valid CreateProductRequestDTO productDTO) {
        ProductResponseDTO productResponseDTO = this.adminService.addProduct(productDTO);
        URI uriLocation = URI.create("/api/v1/products/" + productResponseDTO.id());
        return ResponseEntity.created(uriLocation).body(productResponseDTO);
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@RequestBody @Valid UpdateProductRequestDTO productRequestDTO, @PathVariable UUID id) {
        ProductResponseDTO productResponseDTO = this.adminService.updateProduct(productRequestDTO, id);

        return ResponseEntity.ok(productResponseDTO);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@AuthenticationPrincipal Jwt principal) {
        String uid = principal.getClaimAsString("uid");
        UUID userId = UUID.fromString(uid);

        List<UserResponseDTO> allUsers = this.adminService.getAllUsers(userId);

        return ResponseEntity.ok(allUsers);
    }
}
