package com.aolda.ojakgyo.dto.price;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PricePredictionRequestDto {
    @NotBlank(message = "농작물 이름은 필수 입력값입니다.")
    private String cropName;
} 