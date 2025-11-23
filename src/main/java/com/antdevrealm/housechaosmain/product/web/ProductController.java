package com.antdevrealm.housechaosmain.product.web;

import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable UUID id) {
        ProductResponseDTO productById = productService.getById(id);
        return ResponseEntity.ok(productById);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(@PathVariable UUID id) {
        List<ProductResponseDTO> productsByCategory = this.productService.getAllByCategoryId(id);

        return ResponseEntity.ok(productsByCategory);
    }

}
