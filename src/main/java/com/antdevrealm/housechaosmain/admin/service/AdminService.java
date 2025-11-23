package com.antdevrealm.housechaosmain.admin.service;

import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final ProductService productService;

    public AdminService(ProductService productService) {
        this.productService = productService;
    }

    public ProductResponseDTO addProduct(CreateProductRequestDTO dto) {
        return this.productService.create(dto);
    }
}
