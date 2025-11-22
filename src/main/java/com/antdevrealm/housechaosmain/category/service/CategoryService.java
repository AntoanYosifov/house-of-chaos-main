package com.antdevrealm.housechaosmain.category.service;

import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public List<CategoryResponseDTO> getAll() {
        List<CategoryEntity> allCategories = this.categoryRepository.findAll();

        if(allCategories.isEmpty()) {
            return new ArrayList<>();
        }

        return allCategories.stream().map(c -> new CategoryResponseDTO(c.getId(), c.getName())).toList();
    }

}
