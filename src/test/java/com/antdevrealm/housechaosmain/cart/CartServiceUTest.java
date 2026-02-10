package com.antdevrealm.housechaosmain.cart;

import com.antdevrealm.housechaosmain.cart.dto.CartResponseDTO;
import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
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
    private CloudinaryService cloudinaryService;

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
        String mockPublicId = "house-of-chaos/chair/test-chair-id";
        String mockThumbUrl = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-chair-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Chair")
                .imagePublicId(mockPublicId)
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
        when(cloudinaryService.buildThumbUrl(mockPublicId)).thenReturn(mockThumbUrl);

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

    @Test
    void givenNewItemToCart_whenAddOneToCart_thenNewCartItemIsCreated() {
        UUID ownerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/lamp/test-lamp-id";
        String mockThumbUrl = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-lamp-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Lamp")
                .imagePublicId(mockPublicId)
                .quantity(10)
                .build();

        CartItemEntity newItem = CartItemEntity.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .product(product)
                .quantity(1)
                .build();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(newItem);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(newItem));
        when(cloudinaryService.buildThumbUrl(mockPublicId)).thenReturn(mockThumbUrl);

        CartResponseDTO result = cartService.addOneToCart(ownerId, productId);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(1);

        verify(cartItemRepository, times(1)).save(any(CartItemEntity.class));
    }

    @Test
    void givenExistingItemInCart_whenAddOneToCart_thenQuantityIsIncremented() {
        UUID ownerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/desk/test-desk-id";
        String mockThumbUrl = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-desk-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Desk")
                .imagePublicId(mockPublicId)
                .quantity(10)
                .build();

        CartItemEntity existingItem = CartItemEntity.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(existingItem));
        when(cloudinaryService.buildThumbUrl(mockPublicId)).thenReturn(mockThumbUrl);

        CartResponseDTO result = cartService.addOneToCart(ownerId, productId);

        assertThat(result).isNotNull();
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(3);

        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void givenNonExistentCart_whenAddOneToCart_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addOneToCart(ownerId, productId));

        verify(cartRepository, times(1)).findByOwnerId(ownerId);
        verify(productRepository, never()).findById(any());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void givenNonExistentProduct_whenAddOneToCart_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addOneToCart(ownerId, productId));

        verify(cartRepository, times(1)).findByOwnerId(ownerId);
        verify(productRepository, times(1)).findById(productId);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void givenQuantityExceedsStock_whenAddOneToCart_thenBusinessRuleExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/table/test-table-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Table")
                .imagePublicId(mockPublicId)
                .quantity(3)
                .build();

        CartItemEntity existingItem = CartItemEntity.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .product(product)
                .quantity(3)
                .build();

        when(cartRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existingItem));

        assertThrows(BusinessRuleException.class, () -> cartService.addOneToCart(ownerId, productId));

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void givenItemWithQuantityGreaterThanOne_whenDecreaseItemQuantity_thenQuantityIsDecremented() {
        UUID ownerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/chair/test-chair-id";
        String mockThumbUrl = "https://res.cloudinary.com/test/image/upload/w_400,h_400,c_fill/test-chair-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Chair")
                .imagePublicId(mockPublicId)
                .build();

        CartItemEntity cartItem = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cart)
                .product(product)
                .quantity(3)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem));
        when(cloudinaryService.buildThumbUrl(mockPublicId)).thenReturn(mockThumbUrl);

        CartResponseDTO result = cartService.decreaseItemQuantity(ownerId, cartItemId);

        assertThat(cartItem.getQuantity()).isEqualTo(2);
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);

        verify(cartItemRepository, times(1)).save(cartItem);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void givenItemWithQuantityOne_whenDecreaseItemQuantity_thenItemIsDeleted() {
        UUID ownerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/lamp/test-lamp-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Lamp")
                .imagePublicId(mockPublicId)
                .build();

        CartItemEntity cartItem = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cart)
                .product(product)
                .quantity(1)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of());

        CartResponseDTO result = cartService.decreaseItemQuantity(ownerId, cartItemId);

        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();

        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void givenNonExistentCartItem_whenDecreaseItemQuantity_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.decreaseItemQuantity(ownerId, cartItemId));

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void givenOwnerIdMismatch_whenDecreaseItemQuantity_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID differentOwnerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(differentOwnerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/desk/test-desk-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Desk")
                .imagePublicId(mockPublicId)
                .build();

        CartItemEntity cartItem = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        assertThrows(ResourceNotFoundException.class, () -> cartService.decreaseItemQuantity(ownerId, cartItemId));

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void givenValidCartItem_whenDeleteItem_thenItemIsDeleted() {
        UUID ownerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/table/test-table-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Table")
                .imagePublicId(mockPublicId)
                .build();

        CartItemEntity cartItem = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of());

        CartResponseDTO result = cartService.deleteItem(ownerId, cartItemId);

        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();

        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, times(1)).findAllByCart(cart);
    }

    @Test
    void givenNonExistentCartItem_whenDeleteItem_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteItem(ownerId, cartItemId));

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void givenOwnerIdMismatch_whenDeleteItem_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID differentOwnerId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(differentOwnerId)
                .build();

        CartEntity cart = CartEntity.builder()
                .id(cartId)
                .owner(owner)
                .build();

        String mockPublicId = "house-of-chaos/couch/test-couch-id";
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Test Couch")
                .imagePublicId(mockPublicId)
                .build();

        CartItemEntity cartItem = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cart)
                .product(product)
                .quantity(1)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteItem(ownerId, cartItemId));

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, never()).delete(any());
    }
}
