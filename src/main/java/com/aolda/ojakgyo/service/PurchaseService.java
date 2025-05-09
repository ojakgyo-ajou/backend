package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.PageResp;
import com.aolda.ojakgyo.dto.ProductDetailDto;
import com.aolda.ojakgyo.dto.ProductWithReviewScoreDto;
import com.aolda.ojakgyo.repository.ProductQueryDSL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    
    private final ProductQueryDSL productQueryDSL;

    public PageResp<ProductWithReviewScoreDto> findProductsByName(String productName) {
        return productQueryDSL.findProductsByNameOrderByPriceAndReviewScore(productName);
    }

    public ProductDetailDto findProductDetailById(Long productId) {
        return productQueryDSL.findProductDetailById(productId);
    }
} 