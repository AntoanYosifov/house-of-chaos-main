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

    @Test
    void getRequestToGetNew_shouldReturn200() throws Exception {
        UUID ownerId = UUID.randomUUID();

        OrderResponseDTO order1 = new OrderResponseDTO(
                UUID.randomUUID(),
                ownerId,
                OrderStatus.NEW,
                Instant.now(),
                Instant.now(),
                new BigDecimal("100.00"),
                null,
                List.of()
        );

        OrderResponseDTO order2 = new OrderResponseDTO(
                UUID.randomUUID(),
                ownerId,
                OrderStatus.NEW,
                Instant.now(),
                Instant.now(),
                new BigDecimal("200.00"),
                null,
                List.of()
        );

        when(orderService.getNew(ownerId)).thenReturn(List.of(order1, order2));

        MockHttpServletRequestBuilder request = get("/api/v1/orders/new")
                .with(jwt().jwt(jwt -> jwt.claim("uid", ownerId.toString())));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[1].status").value("NEW"));
    }
}
