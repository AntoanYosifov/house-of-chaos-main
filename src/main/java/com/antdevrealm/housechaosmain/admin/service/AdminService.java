package com.antdevrealm.housechaosmain.admin.service;

import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {
    private final ProductService productService;

    public AdminService(ProductService productService) {
        this.productService = productService;
    }

    public ProductResponseDTO addProduct(CreateProductRequestDTO dto) {
        return this.productService.create(dto);
    }

    public ProductResponseDTO updateProduct(UpdateProductRequestDTO dto, UUID id) {
      return  this.productService.update(dto, id);
    }
}
