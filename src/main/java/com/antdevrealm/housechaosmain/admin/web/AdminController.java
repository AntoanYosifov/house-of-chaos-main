package com.antdevrealm.housechaosmain.admin.web;


import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
}
