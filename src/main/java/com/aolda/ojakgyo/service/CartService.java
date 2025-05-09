package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.CartItemDto;
import com.aolda.ojakgyo.entity.Cart;
import com.aolda.ojakgyo.entity.Product;
import com.aolda.ojakgyo.entity.User;
import com.aolda.ojakgyo.repository.CartRepository;
import com.aolda.ojakgyo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addToCart(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        Cart existingCart = cartRepository.findByUserAndProductId(user, productId)
                .orElse(null);

        if (existingCart != null) {
            // 이미 장바구니에 있는 상품이면 수량 업데이트
            existingCart.updateQuantity(existingCart.getQuantity() + quantity);
        } else {
            // 새로운 상품이면 장바구니에 추가
            Cart cart = Cart.builder()
                    .user(user)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartRepository.save(cart);
        }
    }

    public List<CartItemDto> getCartItems(User user) {
        List<Cart> carts = cartRepository.findByUserWithProduct(user);
        return carts.stream()
                .map(cart -> CartItemDto.builder()
                        .productId(cart.getProduct().getId())
                        .productName(cart.getProduct().getName())
                        .price(cart.getProduct().getPrice())
                        .quantity(cart.getQuantity())
                        .totalPrice(cart.getProduct().getPrice() * cart.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateCartItemQuantity(User user, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 없습니다."));
        
        Product product = cart.getProduct();
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        cart.updateQuantity(quantity);
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        cartRepository.deleteByUserAndProductId(user, productId);
    }
} 