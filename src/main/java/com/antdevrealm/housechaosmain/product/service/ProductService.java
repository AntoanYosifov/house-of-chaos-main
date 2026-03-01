package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.dto.CreateProductForm;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
    }

    public ProductResponseDTO getById(UUID id) {
        ProductEntity productEntity = productRepository.findByIdAndIsActiveIsTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with id: %s not found!", id)));
        return mapToResponseDto(productEntity);
    }

    @Transactional
    @Cacheable("all-products")
    public Page<ProductResponseDTO> getAll(UUID categoryId, Pageable pageable) {
        Page<ProductEntity> entities = categoryId != null
                ? this.productRepository.findAllByCategoryAndIsActiveIsTrue(this.categoryService.getById(categoryId), pageable)
                : this.productRepository.findAllByIsActiveIsTrue(pageable);

        return entities.map(this::mapToResponseDto);
    }

    @Cacheable("new-arrivals")
    public Page<ProductResponseDTO> getNewArrivals(Pageable pageable) {
//        return this.productRepository.findTop10NewArrivals().stream().map(this::mapToResponseDto).toList();
        Page<ProductEntity> entities = this.productRepository.findAllByNewArrivalIsTrueAndIsActiveIsTrueOrderByCreatedOnDesc(pageable);
        return entities.map(this::mapToResponseDto);
    }

    @Cacheable("cheapest")
    public Page<ProductResponseDTO> getCheapest(Pageable pageable) {
        Page<ProductEntity> entities = this.productRepository.findAllByIsActiveIsTrueOrderByPriceAsc(pageable);
        return entities.map(this::mapToResponseDto);
    }

    @Transactional
    @CacheEvict(cacheNames = {"all-products", "new-arrivals", "cheapest"}, allEntries = true)
    public ProductResponseDTO create(CreateProductForm productForm, MultipartFile file) throws IOException {

        CategoryEntity category = this.categoryService.getById(productForm.categoryId());

        ProductEntity productEntity = mapToEntity(productForm);
        productEntity.setCategory(category);

        ProductEntity created = this.productRepository.save(productEntity);

        String publicId = cloudinaryService.uploadImage(
                file.getBytes(),
                "house-of-chaos/" + category.getName(),
                created.getId().toString()
        );

        created.setImagePublicId(publicId);
        created.setUpdatedAt(Instant.now());

        ProductEntity updated = this.productRepository.save(created);

        log.info("Product created: id={}, name={}, categoryId={}",
                updated.getId(), updated.getName(), category.getId());
        return mapToResponseDto(updated);
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

    private ProductEntity mapToEntity(CreateProductForm createProductForm) {
        BigDecimal normalizedPrice = createProductForm.price()
                .setScale(2, RoundingMode.HALF_UP);

        return ProductEntity.builder()
                .name(createProductForm.name())
                .description(createProductForm.description())
                .price(normalizedPrice)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .newArrival(true)
                .isActive(true)
                .quantity(createProductForm.quantity())
                .build();
    }
}
