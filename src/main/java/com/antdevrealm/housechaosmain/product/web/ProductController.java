package com.antdevrealm.housechaosmain.product.web;

import com.antdevrealm.housechaosmain.product.service.ProductService;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid CreateProductRequestDTO productDTO) {
        ProductResponseDTO productResponseDTO = productService.create(productDTO);
        URI uriLocation = URI.create("/api/v1/products/" + productResponseDTO.id());
        return ResponseEntity.created(uriLocation).body(productResponseDTO);
    }
}
