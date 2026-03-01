package com.antdevrealm.housechaosmain.product.web;

import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAll(
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable) {
        Page<ProductResponseDTO> allProducts = productService.getAll(categoryId, pageable);
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<Page<ProductResponseDTO>> getNewArrivals(Pageable pageable) {
        Page<ProductResponseDTO> newArrivals = this.productService.getNewArrivals(pageable);

        return ResponseEntity.ok(newArrivals);
    }

    @GetMapping("/top-deals")
    public ResponseEntity<List<ProductResponseDTO>> getTopDeals() {
        List<ProductResponseDTO> cheapest = this.productService.getCheapest();

        return ResponseEntity.ok(cheapest);
    }
}
