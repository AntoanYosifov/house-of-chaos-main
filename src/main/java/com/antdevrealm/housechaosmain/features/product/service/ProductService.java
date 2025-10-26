package com.antdevrealm.housechaosmain.features.product.service;

import com.antdevrealm.housechaosmain.advice.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.features.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.features.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.features.product.web.dto.CreateProductDTO;
import com.antdevrealm.housechaosmain.features.product.web.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponseDTO create(CreateProductDTO productDTO) {
        ProductEntity productEntity = mapToEntity(productDTO);
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
                productEntity.getImageUrl()
        );
    }

    private ProductEntity mapToEntity(CreateProductDTO createProductDTO) {
        return ProductEntity.builder()
                .name(createProductDTO.name())
                .description(createProductDTO.description())
                .price(createProductDTO.price())
                .quantity(createProductDTO.quantity())
                .imageUrl(createProductDTO.imgUrl()).build();
    }
}
