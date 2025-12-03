package com.antdevrealm.housechaosmain.order.web;

import com.antdevrealm.housechaosmain.order.dto.OrderResponseDTO;
import com.antdevrealm.housechaosmain.order.model.enums.OrderStatus;
import com.antdevrealm.housechaosmain.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerApiTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToGetById_shouldReturn200() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderResponseDTO responseDTO = new OrderResponseDTO(
                orderId,
                ownerId,
                OrderStatus.NEW,
                Instant.now(),
                Instant.now(),
                new BigDecimal("299.98"),
                null,
                List.of()
        );

        when(orderService.getById(ownerId, orderId)).thenReturn(responseDTO);

        MockHttpServletRequestBuilder request = get("/api/v1/orders/{id}", orderId)
                .with(jwt().jwt(jwt -> jwt.claim("uid", ownerId.toString())));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.total").value(299.98));
    }
}
