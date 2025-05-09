package com.aolda.ojakgyo.dto;

import com.aolda.ojakgyo.entity.Information;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InformationDto {
    private String itemCategoryCode;
    private String itemCode;
    private String kindCode;
    private String itemName;
    private String kindName;
    private String unit;
    private int size;

    // Information 엔티티를 DTO로 변환하는 정적 메서드
    public static InformationDto fromEntity(Information entity) {
        if (entity == null) {
            return null;
        }
        return InformationDto.builder()
                .itemCategoryCode(entity.getItemCategoryCode())
                .itemCode(entity.getItemCode())
                .kindCode(entity.getKindCode())
                .itemName(entity.getItemName())
                .kindName(entity.getKindName())
                .unit(entity.getUnit())
                .size(entity.getSize())
                .build();
    }
} 