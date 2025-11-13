package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.product.util.ImgUrlExpander;
import com.antdevrealm.housechaosmain.product.web.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.web.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ImgUrlExpander imgUrlExpander;

    @Autowired
    public ProductService(ProductRepository productRepository, ImgUrlExpander imgUrlExpander) {
        this.productRepository = productRepository;
        this.imgUrlExpander = imgUrlExpander;
    }

    public ProductResponseDTO create(CreateProductRequestDTO productDTO) {
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
                imgUrlExpander.toPublicUrl(productEntity.getImageUrl())
        );
    }

    private ProductEntity mapToEntity(CreateProductRequestDTO createProductRequestDTO) {
        return ProductEntity.builder()
                .name(createProductRequestDTO.name())
                .description(createProductRequestDTO.description())
                .price(createProductRequestDTO.price())
                .quantity(createProductRequestDTO.quantity())
                .imageUrl(createProductRequestDTO.imgUrl()).build();
    }
}
