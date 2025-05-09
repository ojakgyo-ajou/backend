package com.aolda.ojakgyo.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeRequestDto {
    @NotBlank(message = "농작물 이름은 필수 입력값입니다.")
    private String cropName;
} 