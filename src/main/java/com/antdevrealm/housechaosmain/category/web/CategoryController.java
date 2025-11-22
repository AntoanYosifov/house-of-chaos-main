package com.antdevrealm.housechaosmain.category.web;

import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        List<CategoryResponseDTO> allCategories = categoryService.getAll();
        return ResponseEntity.ok(allCategories);
    }
}
