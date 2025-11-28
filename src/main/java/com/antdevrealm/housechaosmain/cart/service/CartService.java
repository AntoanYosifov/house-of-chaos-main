package com.antdevrealm.housechaosmain.cart.service;

import com.antdevrealm.housechaosmain.cart.dto.CartItemResponseDTO;
import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
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

    public void createCart(UserEntity owner) {
        CartEntity cartEntity = CartEntity.builder()
                .owner(owner)
                .build();

        CartEntity savedCart = this.cartRepository.save(cartEntity);
        log.info("Cart created: cartId={}, ownerId={}", savedCart.getId(), owner.getId());
    }

    public void clearCartItems(UserEntity user) {
        CartEntity cart = cartRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart for owner %s not found".formatted(user.getId())
                ));

        this.cartItemRepository.deleteAllByCart(cart);
        log.info("Cart items cleared: cartId={}, ownerId={}", cart.getId(), user.getId());
    }

    public CartResponseDTO getCartByOwnerId(UUID ownerId) {
        CartEntity cart = cartRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                       String.format( "Cart for owner with ID: %s not found", ownerId)));

        List<CartItemEntity> items = cartItemRepository.findAllByCart(cart);

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

        if(item.getQuantity() > productEntity.getQuantity()) {
            throw new BusinessRuleException(String.format("Cart item quantity: %d for product with ID: %s can not exceed product available quantity in stock: %d", item.getQuantity(), productEntity.getId() , productEntity.getQuantity()));
        }

        CartItemEntity savedItem = cartItemRepository.save(item);

        List<CartItemEntity> items = cartItemRepository.findAllByCart(cartEntity);
        log.info("Cart updated (add item): cartId={}, ownerId={}, productId={}, cartItemId={}, newQuantity={}, totalItems={}",
                cartEntity.getId(), ownerId, productId, savedItem.getId(), savedItem.getQuantity(), items.size());
        return mapToCartResponseDTO(cartEntity, items);
    }

    public CartResponseDTO decreaseItemQuantity(UUID ownerId, UUID cartItemId) {
        CartItemEntity cartItemEntity = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cart item with ID: %s not found", cartItemId)));

        CartEntity cartEntity = cartItemEntity.getCart();

        if(!cartEntity.getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException(String.format("Cart for owner with ID: %s not found", ownerId));
        }

        int newQuantity = cartItemEntity.getQuantity() - 1;
        if(newQuantity <= 0) {
            cartItemRepository.delete(cartItemEntity);
        } else {
            cartItemEntity.setQuantity(newQuantity);
            cartItemRepository.save(cartItemEntity);
        }

        List<CartItemEntity> items = cartItemRepository.findAllByCart(cartEntity);
        log.info("Cart updated (decrease item): cartId={}, ownerId={}, cartItemId={}, productId={}, totalItems={}",
                cartEntity.getId(), ownerId, cartItemId, cartItemEntity.getProduct().getId(),
                items.size());

        return mapToCartResponseDTO(cartEntity, items);
    }

    public CartResponseDTO deleteItem(UUID ownerId, UUID cartItemId) {
        CartItemEntity cartItemEntity = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Cart item with ID: %s not found", cartItemId)));

        CartEntity cartEntity = cartItemEntity.getCart();

        if(!cartEntity.getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException(String.format("Cart for owner with ID: %s not found", ownerId));
        }

        cartItemRepository.delete(cartItemEntity);

        List<CartItemEntity> items = cartItemRepository.findAllByCart(cartEntity);

        log.info("Cart item deleted: cartId={}, ownerId={}, cartItemId={},  totalItemsAfterDelete={}",
                cartEntity.getId(), ownerId, cartItemId, items.size());
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
