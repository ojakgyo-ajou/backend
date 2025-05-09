package com.aolda.ojakgyo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricePredictionService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${naver.api.client-id}")
    private String naverClientId;

    @Value("${naver.api.client-secret}")
    private String naverClientSecret;

    @Value("${naver.api.news.url}")
    private String naverNewsUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String predictPrice(String cropName) {
        // 뉴스 기사 가져오기
        List<String> newsArticles = getNewsArticles(cropName);
        log.info("가져온 뉴스 기사 목록:");
        newsArticles.forEach(article -> log.info("기사: {}", article));
        
        String newsArticle = String.join("\n", newsArticles);
        return predictPrice(cropName, newsArticle);
    }

    private List<String> getNewsArticles(String cropName) {
        List<String> articles = new ArrayList<>();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String searchQuery = URLEncoder.encode(cropName + " 시세", StandardCharsets.UTF_8);
            String requestUrl = naverNewsUrl + "?query=" + searchQuery + "&display=10&sort=date";
            
            log.info("네이버 뉴스 API 요청 URL: {}", requestUrl);
            
            HttpGet request = new HttpGet(requestUrl);
            request.setHeader("X-Naver-Client-Id", naverClientId);
            request.setHeader("X-Naver-Client-Secret", naverClientSecret);

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                log.info("네이버 API 응답: {}", responseBody);
                
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                JsonNode items = jsonResponse.get("items");
                
                if (items == null || !items.isArray()) {
                    log.error("API 응답에 items 필드가 없거나 배열이 아닙니다. 응답: {}", responseBody);
                    return articles;
                }
                
                items.forEach(item -> {
                    try {
                        String title = item.get("title").asText().replaceAll("<[^>]*>", "");
                        String description = item.get("description").asText().replaceAll("<[^>]*>", "");
                        String link = item.get("link").asText();
                        String pubDate = item.get("pubDate").asText();
                        
                        String article = String.format("제목: %s\n날짜: %s\n내용: %s\n링크: %s", 
                            title, pubDate, description, link);
                        articles.add(article);
                        log.info("가져온 기사: {}", article);
                    } catch (Exception e) {
                        log.error("기사 파싱 중 오류 발생: {}", e.getMessage());
                    }
                });
            }
            
            log.info("가져온 기사 수: {}", articles.size());
            
            if (articles.isEmpty()) {
                log.warn("가져온 기사가 없습니다.");
            }
        } catch (IOException e) {
            log.error("뉴스 가져오기 중 오류 발생: {}", e.getMessage(), e);
        }
        return articles;
    }

    private String predictPrice(String cropName, String newsArticle) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String prompt = String.format("""
                다음 농작물의 시장 가격을 예측해주세요:
                농작물: %s
                관련 뉴스 기사: %s
                
                다음 형식으로만 답변해주세요. 다른 설명은 하지 마세요:
                1. 예상 가격 : [구체적인 금액을 원 단위로 표시. 예: 5,000원/kg]
                2. 판단 근거 : [한 줄로 간단하게]
                3. 참고 기사 링크 : [해당 작물의 가격, 수급, 작황과 직접적으로 관련된 기사만 3개 이내로 선택]
                
                주의사항:
                - 예상 가격은 반드시 구체적인 금액을 원 단위로 표시해주세요.
                - 참고 기사는 반드시 해당 작물의 가격, 수급, 작황과 직접적으로 관련된 기사만 선택해주세요.
                - 관련 없는 기사는 무시하고 관련 있는 기사만 선택해주세요.
                - 예측이 불가능한 경우에도 위의 양식으로만 답변해주세요.
                """, cropName, newsArticle);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Map.of("parts", new Object[]{Map.of("text", prompt)}));
            requestBody.put("generationConfig", Map.of(
                "temperature", 0.3,
                "topK", 40,
                "topP", 0.95,
                "maxOutputTokens", 1024
            ));

            HttpPost request = new HttpPost("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), "UTF-8"));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                log.info("Gemini API 응답: {}", responseBody);
                
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                return jsonResponse.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("가격 예측 중 오류가 발생했습니다.", e);
        }
    }
} 