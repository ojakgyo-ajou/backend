package com.aolda.ojakgyo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KindPriceDto {
    private String itemCode;        // 품목 코드 (Information.itemCode)
    private String kindCode;        // 품종 코드 (Information.kindCode)
    private String kindName;     // 품종명 (Information 엔티티에 있다면 추가)
    private String unit;            // 단위 (Information.unit)
    private Integer todayPrice;     // 금일 가격 (Daily.dpr1)
    private Integer yesterdayPrice; // 전일 가격 (Daily.dpr2)
    private Double priceChangeRate; // 등락율
    private Integer priceDirection;  // 등락 방향 (0: 하락, 1: 상승, 2: 유지)
    private String latestDate;      // 최근 조사일 (Daily.latestDate)
}