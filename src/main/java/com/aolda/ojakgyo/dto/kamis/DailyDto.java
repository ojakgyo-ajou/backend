package com.aolda.ojakgyo.dto.kamis;

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
    // private String itemName;       // 상세 품목명 (필요시 주석 해제)
    private String unit;           // 단위
    private String latestDate;     // 최근 조사일
    private int todayPrice;      // 금일 가격 (기존 dpr1)
    private int yesterdayPrice;  // 전일 가격 (기존 dpr2)

    // 계산될 필드
    private Double priceChangeRate; // 등락율
    private int priceDirection;  // 등락 방향 ("0": 가격 하락, "1": 가격 상승, "2": 등락 없음)

    // 모든 필드를 받는 생성자 (Lombok의 @AllArgsConstructor가 생성하지만, 계산 로직을 위해 명시적으로 추가 가능)
    // Lombok의 @Builder를 사용하면 모든 필드를 받는 생성자가 자동으로 생성되므로,
    // 아래 생성자는 Lombok이 생성하는 것과 중복될 수 있습니다. 
    // 만약 계산 로직을 생성자에서 처리하고 싶다면 @AllArgsConstructor를 제거하고 이 생성자를 사용하거나,
    // 빌더의 build() 메서드에서 처리하는 것을 고려할 수 있습니다.
    // 여기서는 Lombok의 @Builder와 @AllArgsConstructor를 유지하고, 계산 로직은 서비스 레이어나 빌더의 build()에서 처리한다고 가정합니다.
    // 따라서 이 커스텀 생성자는 주석 처리하거나 삭제합니다.
    /*
    public DailyDto(String productName, String unit, String latestDate, int todayPrice, int yesterdayPrice) {
        this.productName = productName;
        this.unit = unit;
        this.latestDate = latestDate;
        this.todayPrice = todayPrice;
        this.yesterdayPrice = yesterdayPrice;

        // 가격 변화율 및 방향 계산 (필요시 여기에 구현)
        if (yesterdayPrice != 0) {
            this.priceChangeRate = ((double) (todayPrice - yesterdayPrice) / yesterdayPrice * 100);
        } else {
            this.priceChangeRate = (todayPrice > 0) ? 100.0 : 0.0; // 어제 가격이 0이면, 오늘 가격이 있으면 100% 상승으로 간주 (정책에 따라 다름)
        }
        this.priceDirection = (this.priceChangeRate > 0) ? 1 : (this.priceChangeRate < 0) ? 0 : 2;
    }
    */
}