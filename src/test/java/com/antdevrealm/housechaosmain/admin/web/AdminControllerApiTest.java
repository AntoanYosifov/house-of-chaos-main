package com.antdevrealm.housechaosmain.admin.web;

import com.antdevrealm.housechaosmain.admin.service.AdminService;
import com.antdevrealm.housechaosmain.product.dto.CreateProductRequestDTO;
import com.antdevrealm.housechaosmain.product.dto.ProductResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .with(jwt().jwt(jwt -> jwt.claim("authorities", java.util.List.of("ROLE_ADMIN"))));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/products/" + productId))
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Chair"))
                .andExpect(jsonPath("$.price").value(149.99));
    }
}
