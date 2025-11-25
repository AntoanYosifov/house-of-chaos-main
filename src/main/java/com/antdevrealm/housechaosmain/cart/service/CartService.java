package com.antdevrealm.housechaosmain.cart.service;

import com.antdevrealm.housechaosmain.cart.dto.CartItemResponseDTO;
import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ImgUrlExpander imgUrlExpander;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, ImgUrlExpander imgUrlExpander) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.imgUrlExpander = imgUrlExpander;
    }

    public CartResponseDTO getCartByOwnerId(UUID ownerId) {
        CartEntity cart = cartRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart for owner %s not found".formatted(ownerId)
                ));

        List<CartItemEntity> items = cartItemRepository.findAllByCartId(cart.getId());

        return mapToCartResponseDTO(cart, items);
    }

    public CartResponseDTO addOneToCart(UUID ownerId, UUID productId) {
        CartEntity cartEntity = cartRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cart for owner ID: %s not found", ownerId)));

        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with ID: %s not found", productId)));

        CartItemEntity item = cartItemRepository
                .findByCartIdAndProductId(cartEntity.getId(), productId)
                .orElse(null);

        if(item == null) {
            item = CartItemEntity.builder()
                    .cart(cartEntity)
                    .product(productEntity)
                    .quantity(1)
                    .build();
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }

        cartItemRepository.save(item);

        List<CartItemEntity> items = cartItemRepository.findAllByCartId(cartEntity.getId());
        return mapToCartResponseDTO(cartEntity, items);
    }


    private CartResponseDTO mapToCartResponseDTO(CartEntity cartEntity,  List<CartItemEntity> items) {


        List<CartItemResponseDTO> itemDTOs = items.stream()
                .map(this::mapToItemResponseDTO)
                .toList();

        return new CartResponseDTO(cartEntity.getId(), cartEntity.getOwner().getId(), itemDTOs);
    }

    private CartItemResponseDTO mapToItemResponseDTO(CartItemEntity cartItemEntity) {
        return new CartItemResponseDTO(cartItemEntity.getId(),
                cartItemEntity.getProduct().getId(),
                cartItemEntity.getProduct().getName(),
                imgUrlExpander.toPublicUrl(cartItemEntity.getProduct().getImageUrl()),
                cartItemEntity.getQuantity());
    }
}
