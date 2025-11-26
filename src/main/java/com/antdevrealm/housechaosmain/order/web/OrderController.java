package com.antdevrealm.housechaosmain.order.web;


import com.antdevrealm.housechaosmain.order.dto.CreateOrderRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.OrderResponseDTO;
import com.antdevrealm.housechaosmain.order.service.OrderService;
import com.antdevrealm.housechaosmain.util.PrincipalUUIDExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@AuthenticationPrincipal Jwt principal,
                                                   @Valid @RequestBody CreateOrderRequestDTO dto) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        OrderResponseDTO orderResponseDTO = this.orderService.create(ownerId, dto);

        URI uriLocation = URI.create("/api/v1/orders/" + orderResponseDTO.id());

        return ResponseEntity.created(uriLocation).body(orderResponseDTO);
    }
}
