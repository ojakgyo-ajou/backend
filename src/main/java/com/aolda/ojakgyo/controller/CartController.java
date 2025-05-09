package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.CartItemDto;
import com.aolda.ojakgyo.entity.User;
import com.aolda.ojakgyo.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        cartService.addToCart(user, productId, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCartItems(@AuthenticationPrincipal User user) {
        List<CartItemDto> cartItems = cartService.getCartItems(user);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> updateCartItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        cartService.updateCartItemQuantity(user, productId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        cartService.removeFromCart(user, productId);
        return ResponseEntity.ok().build();
    }
} 