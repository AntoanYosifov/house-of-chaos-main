package com.antdevrealm.housechaosmain.category.service;

import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.category.exception.CategoryUniqueNameException;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryEntity getById(UUID categoryId) {

        return this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Category with ID: %s not found!", categoryId)));
    }

    @Cacheable("categories")
    public List<CategoryResponseDTO> getAll() {
        List<CategoryEntity> allCategories = this.categoryRepository.findAll();

        if (allCategories.isEmpty()) {
            return new ArrayList<>();
        }

        return allCategories.stream().map(c -> new CategoryResponseDTO(c.getId(), c.getName())).toList();
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO create(CreateCategoryRequestDTO dto) {
        String normalizedName = dto.name().trim().toLowerCase();
        if (categoryRepository.existsByName(normalizedName)) {
            throw new CategoryUniqueNameException(String.format("Category with name: %s already exist", dto.name()));
        }

        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(dto.name())
                .build();

        CategoryEntity saved = this.categoryRepository.save(categoryEntity);

        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return new CategoryResponseDTO(saved.getId(), saved.getName());
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void delete(CategoryEntity categoryEntity) {
        this.categoryRepository.delete(categoryEntity);
        log.info("Category deleted: id={}, name={}", categoryEntity.getId(), categoryEntity.getName());
    }
}
