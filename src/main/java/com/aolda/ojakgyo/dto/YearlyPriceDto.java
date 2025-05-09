package com.aolda.ojakgyo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyPriceDto {
    private String itemCategoryCode;
    private String itemCategoryName;
    private String itemCode;
    private String itemName;
    private String kindCode;
    private String kindName;
    private String unit;
    private int year;
    private Integer averagePrice;
} 