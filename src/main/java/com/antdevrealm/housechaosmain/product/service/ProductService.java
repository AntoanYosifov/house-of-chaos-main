package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.product.util.ImgUrlExpander;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ImgUrlExpander imgUrlExpander;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryService categoryService, ImgUrlExpander imgUrlExpander) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.imgUrlExpander = imgUrlExpander;
    }

    @Transactional
    public ProductResponseDTO create(CreateProductRequestDTO productDTO) {

        CategoryEntity category = this.categoryService.getById(productDTO.categoryId());
        ProductEntity productEntity = mapToEntity(productDTO);
        productEntity.setCategory(category);

        ProductEntity saved = this.productRepository.save(productEntity);
        return mapToResponseDto(saved);
    }

    public ProductResponseDTO getById(UUID id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with id: %s not found!", id)));
        return mapToResponseDto(productEntity);
    }

    private ProductResponseDTO mapToResponseDto(ProductEntity productEntity) {
        return new ProductResponseDTO(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice(),
                productEntity.getQuantity(),
                imgUrlExpander.toPublicUrl(productEntity.getImageUrl())
        );
    }

    private ProductEntity mapToEntity(CreateProductRequestDTO createProductRequestDTO) {
        return ProductEntity.builder()
                .name(createProductRequestDTO.name())
                .description(createProductRequestDTO.description())
                .price(createProductRequestDTO.price())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .newArrival(true)
                .quantity(createProductRequestDTO.quantity())
                .imageUrl(createProductRequestDTO.imgUrl()).build();
    }
}
