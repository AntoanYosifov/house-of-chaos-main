package com.antdevrealm.housechaosmain.cart.web;

import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getByOwnerId(@AuthenticationPrincipal Jwt principal) {
        String uid = principal.getClaimAsString("uid");
        UUID ownerId = UUID.fromString(uid);
        CartResponseDTO cartById = this.cartService.getCartByOwnerId(ownerId);

        return ResponseEntity.ok(cartById);
    }
}
