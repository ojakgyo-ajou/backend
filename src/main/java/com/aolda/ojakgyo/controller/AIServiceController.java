package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.price.PricePredictionRequestDto;
import com.aolda.ojakgyo.dto.recipe.RecipeRequestDto;
import com.aolda.ojakgyo.service.PricePredictionService;
import com.aolda.ojakgyo.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIServiceController {

    private final RecipeService recipeService;
    private final PricePredictionService pricePredictionService;

    @PostMapping("/recipe")
    public ResponseEntity<String> getRecipe(@Valid @RequestBody RecipeRequestDto requestDto) {
        String recipe = recipeService.getRecipe(requestDto.getCropName());
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/price-prediction")
    public ResponseEntity<String> predictPrice(@Valid @RequestBody PricePredictionRequestDto requestDto) {
        String prediction = pricePredictionService.predictPrice(requestDto.getCropName());
        return ResponseEntity.ok(prediction);
    }
} 