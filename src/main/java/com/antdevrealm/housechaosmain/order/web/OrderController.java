package com.antdevrealm.housechaosmain.order.web;


import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.ConfirmedOrderResponseDTO;
import com.antdevrealm.housechaosmain.order.dto.CreateOrderRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.OrderResponseDTO;
import com.antdevrealm.housechaosmain.order.service.OrderService;
import com.antdevrealm.housechaosmain.util.PrincipalUUIDExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        OrderResponseDTO responseDTO = this.orderService.getById(ownerId, id);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/new")
    public ResponseEntity<List<OrderResponseDTO>> getNew(@AuthenticationPrincipal Jwt principal) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        List<OrderResponseDTO> responseDTOS = this.orderService.getNew(ownerId);

        return ResponseEntity.ok(responseDTOS);
    }

    @GetMapping("/confirmed")
    public ResponseEntity<List<OrderResponseDTO>> getConfirmed(@AuthenticationPrincipal Jwt principal) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        List<OrderResponseDTO> responseDTOS = this.orderService.getConfirmed(ownerId);

        return ResponseEntity.ok(responseDTOS);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<OrderResponseDTO>> getCancelled(@AuthenticationPrincipal Jwt principal) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        List<OrderResponseDTO> responseDTOS = this.orderService.getCancelled(ownerId);

        return ResponseEntity.ok(responseDTOS);
    }


    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@AuthenticationPrincipal Jwt principal,
                                                   @Valid @RequestBody CreateOrderRequestDTO dto) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        OrderResponseDTO orderResponseDTO = this.orderService.create(ownerId, dto);

        URI uriLocation = URI.create("/api/v1/orders/" + orderResponseDTO.id());

        return ResponseEntity.created(uriLocation).body(orderResponseDTO);
    }

    @PatchMapping("/confirm/{id}")
    public ResponseEntity<ConfirmedOrderResponseDTO> confirm(@AuthenticationPrincipal Jwt principal,
                                                             @PathVariable UUID id,
                                                             @RequestBody @Valid AddressRequestDTO shippingAddress) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        ConfirmedOrderResponseDTO responseDTO = this.orderService.confirm(ownerId, id, shippingAddress);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<OrderResponseDTO> cancel(@AuthenticationPrincipal Jwt principal,
                                                   @PathVariable UUID id) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);

        OrderResponseDTO responseDTO = this.orderService.cancel(ownerId, id);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt principal,
                                       @PathVariable UUID id) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        this.orderService.delete(ownerId, id);

        return ResponseEntity.noContent().build();
    }

}
