package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.NewsArticleDto;
import com.aolda.ojakgyo.entity.NewsArticle;
import com.aolda.ojakgyo.repository.NewsArticleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NewsArticleService {
    
    @Value("${naver.api.client-id}")
    private String naverClientId;

    @Value("${naver.api.client-secret}")
    private String naverClientSecret;

    @Value("${naver.api.news.url}")
    private String naverNewsUrl;

    private final NewsArticleRepository newsArticleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<NewsArticleDto> getNewsArticles(String cropName) {
        // DB에서 기사 조회
        List<NewsArticle> articles = newsArticleRepository.findTop5ByCropNameOrderByPubDateDesc(cropName);
        
        // DB에 기사가 없으면 새로운 기사 검색 및 저장
        if (articles.isEmpty()) {
            articles = searchAndSaveNewsArticles(cropName);
        }
        
        // DTO로 변환하여 반환
        return articles.stream()
                .map(article -> NewsArticleDto.builder()
                        .title(article.getTitle())
                        .press(article.getPress())
                        .pubDate(article.getPubDate())
                        .link(article.getLink())
                        .build())
                .collect(Collectors.toList());
    }

    protected List<NewsArticle> searchAndSaveNewsArticles(String cropName) {
        List<NewsArticle> articles = new ArrayList<>();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String searchQuery = URLEncoder.encode(cropName, StandardCharsets.UTF_8);
            String requestUrl = naverNewsUrl + "?query=" + searchQuery + "&display=20&sort=sim";
            
            HttpGet request = new HttpGet(requestUrl);
            request.setHeader("X-Naver-Client-Id", naverClientId);
            request.setHeader("X-Naver-Client-Secret", naverClientSecret);

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                JsonNode items = jsonResponse.get("items");
                
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        try {
                            String title = item.get("title").asText().replaceAll("<[^>]*>", "");
                            String press = item.has("publisher") ? item.get("publisher").asText() : "기타";
                            String pubDate = item.has("pubDate") ? 
                                item.get("pubDate").asText().split(" ")[3] + "-" + 
                                getMonthNumber(item.get("pubDate").asText().split(" ")[2]) + "-" + 
                                item.get("pubDate").asText().split(" ")[1] : 
                                "날짜 정보 없음";
                            String link = item.has("link") ? item.get("link").asText() : "";
                            
                            NewsArticle article = NewsArticle.builder()
                                    .cropName(cropName)
                                    .title(title)
                                    .press(press)
                                    .pubDate(pubDate)
                                    .link(link)
                                    .build();
                            
                            articles.add(article);
                        } catch (Exception e) {
                            log.error("기사 파싱 중 오류 발생: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("뉴스 검색 중 오류 발생: {}", e.getMessage(), e);
        }
        
        return newsArticleRepository.saveAll(articles);
    }

    private String getMonthNumber(String month) {
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("Jan", "01");
        monthMap.put("Feb", "02");
        monthMap.put("Mar", "03");
        monthMap.put("Apr", "04");
        monthMap.put("May", "05");
        monthMap.put("Jun", "06");
        monthMap.put("Jul", "07");
        monthMap.put("Aug", "08");
        monthMap.put("Sep", "09");
        monthMap.put("Oct", "10");
        monthMap.put("Nov", "11");
        monthMap.put("Dec", "12");
        return monthMap.getOrDefault(month, "01");
    }
} 