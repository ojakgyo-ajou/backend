package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.PageResp;
import com.aolda.ojakgyo.dto.ProductDetailDto;
import com.aolda.ojakgyo.dto.ProductWithReviewScoreDto;
import com.aolda.ojakgyo.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/products")
    public ResponseEntity<PageResp<ProductWithReviewScoreDto>> findProductsByName(
            @RequestParam(required = true) String productName) {
        return ResponseEntity.ok(purchaseService.findProductsByName(productName));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDetailDto> findProductDetailById(
            @PathVariable Long productId) {
        return ResponseEntity.ok(purchaseService.findProductDetailById(productId));
    }
} 