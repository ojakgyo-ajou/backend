package com.aolda.ojakgyo.dto;

import com.aolda.ojakgyo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithReviewScoreDto {
    private Long productId;
    private String userId;
    private String name;
    private String productName;
    private String productPrice;
    private Double averageScore;
} 