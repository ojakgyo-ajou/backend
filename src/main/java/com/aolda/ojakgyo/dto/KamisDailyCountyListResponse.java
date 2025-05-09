package com.aolda.ojakgyo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KamisDailyCountyListResponse {
    // condition 필드는 String 배열의 배열 형태이므로, 정확한 파싱을 위해 List<List<String>> 또는 사용자 정의 객체 사용
    // 예시에서는 간단히 Object로 두거나, 실제 사용하지 않는다면 무시할 수 있습니다.
    // private List<List<String>> condition;
    private Object condition; // 또는 List<List<String>>

    @JsonProperty("error_code")
    private String errorCode;

    private List<PriceDetailItem> price; // price 키에 해당하는 배열

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class PriceDetailItem {
        @JsonProperty("county_code")
        private String countyCode;
        @JsonProperty("county_name")
        private String countyName;
        @JsonProperty("product_cls_code")
        private List<String> productClsCode; // 배열 형태
        @JsonProperty("product_cls_name")
        private List<String> productClsName; // 배열 형태
        @JsonProperty("category_code")
        private String categoryCode;
        @JsonProperty("category_name")
        private String categoryName;
        private String productno; // 품목 코드 (API 문서상)
        @JsonProperty("lastest_day")
        private String lastestDay; // API 문서에는 lastest_date, 실제 응답에는 lastest_day
        private String productName;
        @JsonProperty("item_name")
        private String itemName;
        private String unit;
        private String day1;
        private String dpr1; // 당일 가격 (쉼표 포함 문자열)
        private String day2;
        private String dpr2; // 1일전 가격 (쉼표 포함 문자열)
        private String day3;
        private Object dpr3; // 1개월전 가격 (쉼표 또는 빈 배열 [] 가능)
        private String day4;
        private Object dpr4; // 1년전 가격 (쉼표 또는 빈 배열 [] 가능)
        private String direction; // 등락여부 (0:하락, 1:상승, 2:유지)
        private String value; // 등락율 (문자열)
        @JsonProperty("result_code") // API 문서에만 있고 실제 응답에 없을 수 있음
        private String resultCode;
    }
}