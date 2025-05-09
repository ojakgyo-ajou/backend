package com.aolda.ojakgyo.dto;

import com.aolda.ojakgyo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {
    private Long productId;
    private String productName;
    private String productPrice;
    private String userId;
    private String name;
    private Double sellerAverageScore;
    private Double productAverageScore;
    private List<ReviewDto> reviews;
}
