package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.product.util.ImgUrlExpander;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ImgUrlExpander imgUrlExpander;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          ImgUrlExpander imgUrlExpander) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.imgUrlExpander = imgUrlExpander;
    }

    public ProductResponseDTO getById(UUID id) {
        ProductEntity productEntity = productRepository.findByIdAndIsActiveIsTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with id: %s not found!", id)));
        return mapToResponseDto(productEntity);
    }

    @Transactional
    public List<ProductResponseDTO> getAllByCategoryId(UUID categoryId) {
        CategoryEntity category = this.categoryService.getById(categoryId);

        List<ProductEntity> allByCategory = this.productRepository.findAllByCategoryAndIsActiveIsTrue(category);

        return allByCategory.stream().map(this::mapToResponseDto).toList();
    }

    @Transactional
    public ProductResponseDTO create(CreateProductRequestDTO productDTO) {

        CategoryEntity category = this.categoryService.getById(productDTO.categoryId());
        ProductEntity productEntity = mapToEntity(productDTO);
        productEntity.setCategory(category);

        ProductEntity saved = this.productRepository.save(productEntity);
        return mapToResponseDto(saved);
    }

    @Transactional
    public ProductResponseDTO update(UpdateProductRequestDTO dto, UUID productId) {
        ProductEntity productEntity = this.productRepository.findByIdAndIsActiveIsTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with ID: %s not found!", productId)));

        productEntity.setDescription(dto.description());
        productEntity.setPrice(dto.price());

        ProductEntity updated = this.productRepository.save(productEntity);

        return mapToResponseDto(updated);
    }

    @Transactional
    public void softDelete(UUID productId) {
        ProductEntity productEntity = this.productRepository.findByIdAndIsActiveIsTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with ID: %s not found!", productId)));

        productEntity.setActive(false);
        this.productRepository.save(productEntity);
    }

    public boolean existsByCategory(CategoryEntity category) {
        return this.productRepository.existsByCategory(category);
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
                .isActive(true)
                .quantity(createProductRequestDTO.quantity())
                .imageUrl(createProductRequestDTO.imgUrl()).build();
    }

}
