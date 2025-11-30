package com.antdevrealm.housechaosmain.category;

import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.category.exception.CategoryUniqueNameException;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.category.service.CategoryService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
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

    @Test
    void givenCategoriesExist_whenGetAll_thenListOfCategoryResponseDTOsIsReturned() {
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        CategoryEntity category1 = CategoryEntity.builder()
                .id(categoryId1)
                .name("Furniture")
                .build();

        CategoryEntity category2 = CategoryEntity.builder()
                .id(categoryId2)
                .name("Electronics")
                .build();

        List<CategoryEntity> categories = List.of(category1, category2);

        when(categoryRepository.findAll()).thenReturn(categories);

        List<CategoryResponseDTO> result = categoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(categoryId1);
        assertThat(result.get(0).name()).isEqualTo("Furniture");
        assertThat(result.get(1).id()).isEqualTo(categoryId2);
        assertThat(result.get(1).name()).isEqualTo("Electronics");

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void givenNoCategoriesExist_whenGetAll_thenEmptyListIsReturned() {
        when(categoryRepository.findAll()).thenReturn(new ArrayList<>());

        List<CategoryResponseDTO> result = categoryService.getAll();

        assertThat(result).isEmpty();

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void givenUniqueCategoryName_whenCreate_thenCategoryIsCreatedAndReturned() {
        String uniqueCategoryName = "furniture";

        CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO(uniqueCategoryName);

        UUID generatedId = UUID.randomUUID();
        CategoryEntity savedEntity = CategoryEntity.builder()
                .id(generatedId)
                .name(uniqueCategoryName)
                .build();

        when(categoryRepository.existsByName(uniqueCategoryName)).thenReturn(false);

        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(savedEntity);

        CategoryResponseDTO result = categoryService.create(requestDTO);

        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.name()).isEqualTo(uniqueCategoryName);

        verify(categoryRepository, times(1)).existsByName(uniqueCategoryName);
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void givenCategoryNameAlreadyExists_whenCreate_thenCategoryUniqueNameExceptionIsThrown() {
        String existingCategoryName = "furniture";

        CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO(existingCategoryName);

        when(categoryRepository.existsByName(existingCategoryName)).thenReturn(true);

        assertThrows(CategoryUniqueNameException.class, () -> categoryService.create(requestDTO));

        verify(categoryRepository, times(1)).existsByName(existingCategoryName);
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void givenCategoryEntity_whenDelete_thenCategoryIsDeleted() {
        UUID categoryId = UUID.randomUUID();
        String categoryName = "Furniture";

        CategoryEntity categoryEntity = CategoryEntity.builder()
                .id(categoryId)
                .name(categoryName)
                .build();

        categoryService.delete(categoryEntity);

        verify(categoryRepository, times(1)).delete(categoryEntity);
    }
}
