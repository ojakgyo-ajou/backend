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

    // Daily 엔티티에서 가져올 주요 품목 정보
    private String productName;    // 품목명
    private String itemName;       // 상세 품목명 (또는 부류 + 품목 조합)
    private String unit;           // 단위
    private String latestDate;     // 최근 조사일
    private int dpr1;      // 금일 가격 (예: "10,000")
    private int dpr2;      // 전일 가격 (예: "12,000")

    // 계산될 필드
    private Double priceChangeRate; // 등락율 ("-16.7" 이면 16.7% 하락, "5.0" 이면 5.0% 상승)
    private int priceDirection;  // 등락 방향 ("0": 가격 하락, "1": 가격 상승, "2": 등락 없음)

    public DailyDto(String productName, String itemName, String unit, String latestDate, int dpr1, int dpr2) {
        this.productName = productName;
        this.itemName = itemName;
        this.unit = unit;
        this.latestDate = latestDate;
        this.dpr1 = dpr1;
        this.dpr2 = dpr2;

        // 가격 변화율 계산
        this.priceChangeRate = ((double) (dpr1 - dpr2) / dpr2 * 100);
        this.priceDirection = (priceChangeRate > 0) ? 1 : (priceChangeRate < 0) ? 0 : 2; // 상승: 1, 하락: 0, 유지 : 2
    }
}