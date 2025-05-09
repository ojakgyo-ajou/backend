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

    @GetMapping("/recipe")
    public ResponseEntity<String> getRecipe(@RequestParam(required = true) String productName) {
        String recipe = recipeService.getRecipe(productName);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/price-prediction")
    public ResponseEntity<Object> predictPrice(@RequestParam(required = true) String productName) {
        return ResponseEntity.ok(pricePredictionService.predictPrice(productName));
    }
} 