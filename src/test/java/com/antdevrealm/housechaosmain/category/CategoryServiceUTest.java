package com.antdevrealm.housechaosmain.category;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void givenExistingCategoryId_whenGetById_thenCategoryIsReturned() {
        UUID categoryId = UUID.randomUUID();

        CategoryEntity categoryEntity = CategoryEntity.builder()
                .id(categoryId)
                .name("Furniture")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryEntity));

        CategoryEntity result = categoryService.getById(categoryId);

        assertThat(result).isEqualTo(categoryEntity);
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("Furniture");

        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void givenNonExistentCategoryId_whenGetById_thenResourceNotFoundExceptionIsThrown() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getById(categoryId));

        verify(categoryRepository, times(1)).findById(categoryId);
    }
}
