package com.antdevrealm.housechaosmain.admin.web;

import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.category.dto.CategoryResponseDTO;
import com.antdevrealm.housechaosmain.category.dto.CreateCategoryRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.antdevrealm.housechaosmain.product.dto.UpdateProductRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerApiTest {

    @MockitoBean
    private AdminService adminService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postAuthorizedRequestToAddProduct_shouldReturn201Created() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO(
                "Test Chair",
                "Test description for chair",
                new BigDecimal("149.99"),
                5,
                "http://example.com/chair.jpg",
                categoryId
        );

        UUID productId = UUID.randomUUID();
        ProductResponseDTO responseDTO = new ProductResponseDTO(
                productId,
                "Test Chair",
                "Test description for chair",
                new BigDecimal("149.99"),
                5,
                "http://example.com/chair.jpg"
        );

        when(adminService.addProduct(any(CreateProductRequestDTO.class))).thenReturn(responseDTO);

        MockHttpServletRequestBuilder request = post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
                .with(jwt().jwt(jwt -> jwt.claim("authorities", List.of("ROLE_ADMIN"))));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/products/" + productId))
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Chair"))
                .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    void patchAuthorizedRequestToUpdateProduct_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        UpdateProductRequestDTO requestDTO = new UpdateProductRequestDTO(
                "Updated test description for lamp",
                new BigDecimal("399.99")
        );

        ProductResponseDTO responseDTO = new ProductResponseDTO(
                productId,
                "Test Lamp",
                "Updated test description for lamp",
                new BigDecimal("399.99"),
                2,
                "http://example.com/lamp.jpg"
        );

        when(adminService.updateProduct(any(UpdateProductRequestDTO.class), eq(productId))).thenReturn(responseDTO);

        MockHttpServletRequestBuilder request = patch("/api/v1/admin/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
                .with(jwt().jwt(jwt -> jwt.claim("authorities", List.of("ROLE_ADMIN"))));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.description").value("Updated test description for lamp"))
                .andExpect(jsonPath("$.price").value(399.99));
    }

    @Test
    void deleteAuthorizedRequestToDeleteProduct_shouldReturn204() throws Exception {
        UUID productId = UUID.randomUUID();

        doNothing().when(adminService).deleteProduct(productId);

        MockHttpServletRequestBuilder request = delete("/api/v1/admin/products/{id}", productId)
                .with(jwt().jwt(jwt -> jwt.claim("authorities", List.of("ROLE_ADMIN"))));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        verify(adminService, times(1)).deleteProduct(productId);
    }

    @Test
    void postAuthorizedRequestToAddCategory_shouldReturn200() throws Exception {
        CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO("table");

        UUID categoryId = UUID.randomUUID();
        CategoryResponseDTO responseDTO = new CategoryResponseDTO(categoryId, "table");

        when(adminService.addCategory(any(CreateCategoryRequestDTO.class))).thenReturn(responseDTO);

        MockHttpServletRequestBuilder request = post("/api/v1/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
                .with(jwt().jwt(jwt -> jwt.claim("authorities", List.of("ROLE_ADMIN"))));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("table"));
    }
}
