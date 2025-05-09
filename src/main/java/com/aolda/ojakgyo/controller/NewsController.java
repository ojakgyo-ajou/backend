package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.NewsArticleDto;
import com.aolda.ojakgyo.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    public ResponseEntity<List<NewsArticleDto>> getNewsArticles(@RequestParam(required = true) String productName) {
        List<NewsArticleDto> articles = newsArticleService.getNewsArticles(productName);
        return ResponseEntity.ok(articles);
    }
} 