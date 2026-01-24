package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    private final ImgUrlExpander imgUrlExpander;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService, CloudinaryService cloudinaryService,
                          ImgUrlExpander imgUrlExpander) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
        this.imgUrlExpander = imgUrlExpander;
    }

    public ProductResponseDTO getById(UUID id) {
        ProductEntity productEntity = productRepository.findByIdAndIsActiveIsTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with id: %s not found!", id)));
        return mapToResponseDto(productEntity);
    }

    @Transactional
    @Cacheable("by-category")
    public List<ProductResponseDTO> getAllByCategoryId(UUID categoryId) {
        CategoryEntity category = this.categoryService.getById(categoryId);

        List<ProductEntity> allByCategory = this.productRepository.findAllByCategoryAndIsActiveIsTrue(category);

        return allByCategory.stream().map(this::mapToResponseDto).toList();
    }
    @Cacheable("new-arrivals")
    public List<ProductResponseDTO> getNewArrivals() {
        return this.productRepository.findTop10NewArrivals().stream().map(this::mapToResponseDto).toList();
    }

    @Cacheable("cheapest")
    public List<ProductResponseDTO> getCheapest() {
        return this.productRepository.findTop10Cheapest().stream().map(this::mapToResponseDto).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = {"by-category", "new-arrivals", "cheapest"}, allEntries = true)
    public ProductResponseDTO create(CreateProductRequestDTO productDTO) {

        CategoryEntity category = this.categoryService.getById(productDTO.categoryId());
        ProductEntity productEntity = mapToEntity(productDTO);
        productEntity.setCategory(category);

        ProductEntity saved = this.productRepository.save(productEntity);

        log.info("Product created: id={}, name={}, categoryId={}",
                saved.getId(), saved.getName(), category.getId());
        return mapToResponseDto(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = {"by-category", "new-arrivals", "cheapest"}, allEntries = true)
    public ProductResponseDTO update(UpdateProductRequestDTO dto, UUID productId) {
        ProductEntity productEntity = this.productRepository.findByIdAndIsActiveIsTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with ID: %s not found!", productId)));

        productEntity.setDescription(dto.description());
        productEntity.setPrice(dto.price());

        ProductEntity updated = this.productRepository.save(productEntity);
        log.info("Product updated: id={}, newPrice={}, newDescriptionLength={}",
                updated.getId(), updated.getPrice(), updated.getDescription().length());

        return mapToResponseDto(updated);
    }

    @Transactional
    @CacheEvict(cacheNames = {"by-category", "new-arrivals", "cheapest"}, allEntries = true)
    public void softDelete(UUID productId) {
        ProductEntity productEntity = this.productRepository.findByIdAndIsActiveIsTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with ID: %s not found!", productId)));

        productEntity.setActive(false);
        this.productRepository.save(productEntity);
        log.info("Product soft-deleted: id={}, name={}", productEntity.getId(), productEntity.getName());
    }

    public boolean existsByCategory(CategoryEntity category) {
        return this.productRepository.existsByCategory(category);
    }

    @Transactional
    @CacheEvict(cacheNames = {"by-category", "new-arrivals", "cheapest"}, allEntries = true)
    public int markOldNewArrivalsAsNotNew(int daysAsNew) {
        Instant threshold = Instant.now().minus(Duration.ofDays(daysAsNew));
        return productRepository.markOldNewArrivalsAsNotNew(threshold);
    }

    private ProductResponseDTO mapToResponseDto(ProductEntity productEntity) {
        return new ProductResponseDTO(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice(),
                productEntity.getQuantity(),
                cloudinaryService.buildThumbUrl(productEntity.getImagePublicId()),
                cloudinaryService.buildLargeUrl(productEntity.getImagePublicId())
        );
    }

    private ProductEntity mapToEntity(CreateProductRequestDTO createProductRequestDTO) {
        BigDecimal normalizedPrice = createProductRequestDTO.price()
                .setScale(2, RoundingMode.HALF_UP);

        return ProductEntity.builder()
                .name(createProductRequestDTO.name())
                .description(createProductRequestDTO.description())
                .price(normalizedPrice)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .newArrival(true)
                .isActive(true)
                .quantity(createProductRequestDTO.quantity())
                .imageUrl(createProductRequestDTO.imgUrl()).build();
    }
}
