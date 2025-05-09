package com.aolda.ojakgyo.dto.kamis;

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
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 있지만 DTO에 없는 필드는 무시
@ToString
public class KamisApiResponse {
    private Condition condition;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class Condition {
        private ConditionItem item;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class ConditionItem {
        @JsonProperty("p_startday")
        private String pStartday;
        @JsonProperty("p_endday")
        private String pEndday;
        @JsonProperty("p_itemcategorycode")
        private String pItemcategorycode;
        @JsonProperty("p_itemcode")
        private String pItemcode;
        @JsonProperty("p_kindcode")
        private String pKindcode;
        @JsonProperty("p_productrankcode")
        private String pProductrankcode;
        @JsonProperty("p_countycode")
        private String pCountycode; // null일 수 있음
        @JsonProperty("p_convert_kg_yn")
        private String pConvertKgYn;
        @JsonProperty("p_key")
        private String pKey;
        @JsonProperty("p_id")
        private String pId;
        @JsonProperty("p_returntype")
        private String pReturntype;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class Data {
        @JsonProperty("error_code")
        private String errorCode;
        private List<PriceItem> item; // price 데이터 리스트
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class PriceItem {
        private String itemname; // "쌀" 또는 null
        private String kindname; // "20kg(1kg)" 또는 null
        private String countyname; // "평균", "평년", "서울" 등
        private String marketname; // "양곡도매" 또는 null
        private String yyyy;
        private String regday; // "MM/dd" 형식
        private String price;  // "2,434" 형식 (쉼표 포함)
    }
}