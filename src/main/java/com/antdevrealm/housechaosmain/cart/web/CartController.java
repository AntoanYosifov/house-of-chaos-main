package com.antdevrealm.housechaosmain.cart.web;

import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.util.PrincipalUUIDExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        CartResponseDTO cartById = this.cartService.getCartByOwnerId(ownerId);

        return ResponseEntity.ok(cartById);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponseDTO> upsert(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        CartResponseDTO cartResponseDTO = this.cartService.addOneToCart(ownerId, id);

        return ResponseEntity.ok(cartResponseDTO);
    }

    @PostMapping("/items/{id}/decrease")
    public ResponseEntity<CartResponseDTO> decrease(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        CartResponseDTO cartResponseDTO = this.cartService.decreaseItemQuantity(ownerId, id);

        return ResponseEntity.ok(cartResponseDTO);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponseDTO> delete(@AuthenticationPrincipal Jwt principal, @PathVariable UUID id) {
        UUID ownerId = PrincipalUUIDExtractor.extract(principal);
        CartResponseDTO cartResponseDTO = this.cartService.deleteItem(ownerId, id);

        return ResponseEntity.ok(cartResponseDTO);
    }
}
