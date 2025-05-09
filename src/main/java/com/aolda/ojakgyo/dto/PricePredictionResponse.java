package com.aolda.ojakgyo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePredictionResponse {
    private String reason;  // 판단 근거
    private int priceLevel; // 0: 싸다, 1: 보통, 2: 비싸다
} 