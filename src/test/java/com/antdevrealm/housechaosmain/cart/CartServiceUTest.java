package com.antdevrealm.housechaosmain.cart;

import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceUTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImgUrlExpander imgUrlExpander;

    @InjectMocks
    private CartService cartService;

    @Test
    void givenExistingCart_whenGetCartByOwnerId_thenCartResponseDTOIsReturned() {
        UUID ownerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        UUID productId = UUID.randomUUID();
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Chair")
                .imageUrl("/images/chair.jpg")
                .build();

        UUID cartItemId = UUID.randomUUID();
        CartItemEntity cartItem = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem));
        when(imgUrlExpander.toPublicUrl("/images/chair.jpg")).thenReturn("http://localhost:8080/images/chair.jpg");

        CartResponseDTO result = cartService.getCartByOwnerId(ownerId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(cartId);
        assertThat(result.ownerId()).isEqualTo(ownerId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).id()).isEqualTo(cartItemId);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);

        verify(cartRepository, times(1)).findByOwnerId(ownerId);
        verify(cartItemRepository, times(1)).findAllByCart(cart);
    }

    @Test
    void givenNonExistentOwnerId_whenGetCartByOwnerId_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.getCartByOwnerId(ownerId));

        verify(cartRepository, times(1)).findByOwnerId(ownerId);
        verify(cartItemRepository, never()).findAllByCart(any());
    }
}
