package com.antdevrealm.housechaosmain.cart.service;

import com.antdevrealm.housechaosmain.cart.dto.CartItemResponseDTO;
import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public CartResponseDTO getCartByOwnerId(UUID id) {
        CartEntity cartEntity = this.cartRepository.findByOwnerId(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cart with owner ID: %s not found!", id)));

        return mapToCartResponseDTO(cartEntity);
    }

    private CartResponseDTO mapToCartResponseDTO(CartEntity cartEntity) {
        List<CartItemResponseDTO> itemDTOList;

        if(cartEntity.getItems().isEmpty()) {
            itemDTOList = new ArrayList<>();
        } else {
            itemDTOList = cartEntity.getItems().stream().map(this::mapToItemResponseDTO).toList();
        }
        return new CartResponseDTO(cartEntity.getId(), cartEntity.getOwner().getId(), itemDTOList);
    }

    private CartItemResponseDTO mapToItemResponseDTO(CartItemEntity cartItemEntity) {
        return new CartItemResponseDTO(cartItemEntity.getId(),
                cartItemEntity.getProduct().getId(),
                cartItemEntity.getQuantity());
    }
}
