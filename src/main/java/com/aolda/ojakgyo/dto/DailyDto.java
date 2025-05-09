package com.aolda.ojakgyo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDto {
    private String productName;    // API의 productName 또는 itemName
    private String unit;           // API의 unit
    private String latestDate;     // API의 lastest_day
    private int todayPrice;        // API의 dpr1 (정수형으로 변환)
    private int yesterdayPrice;    // API의 dpr2 (정수형으로 변환)
    private Double priceChangeRate; // API의 value (Double형으로 변환, 하락 시 음수)
    private int priceDirection;    // API의 direction (정수형으로 변환)

    // 필요하다면 Information 엔티티의 다른 정보도 추가 가능
    private String itemCategoryName; // 예: Information의 itemCategoryName
    private String kindName;         // 예: Information의 kindName
}