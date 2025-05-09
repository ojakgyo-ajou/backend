package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.NewsArticleDto;
import com.aolda.ojakgyo.dto.PricePredictionResponse;
import com.aolda.ojakgyo.entity.NewsArticle;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final NewsArticleService newsArticleService;
    private static final int MAX_RETRIES = 3;

    @Transactional
    public List<NewsArticle> searchAndSaveNewsArticles(String cropName) {
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
                    List<NewsArticle> allArticles = new ArrayList<>();
                    
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
                            
                            allArticles.add(article);
                    } catch (Exception e) {
                        log.error("기사 파싱 중 오류 발생: {}", e.getMessage());
                        }
                    }
                    
                    // LLM을 사용하여 관련 기사 필터링
                    articles = checkArticleRelevanceWithLLM(cropName, allArticles);
            }
            }
        } catch (Exception e) {
            log.error("뉴스 검색 중 오류 발생: {}", e.getMessage(), e);
        }
        return articles;
    }

    private List<NewsArticle> checkArticleRelevanceWithLLM(String cropName, List<NewsArticle> articles) {
        List<NewsArticle> relevantArticles = new ArrayList<>();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            StringBuilder articlesText = new StringBuilder();
            for (NewsArticle article : articles) {
                articlesText.append(String.format("제목: %s\n신문사: %s\n날짜: %s\n\n", 
                    article.getTitle(), article.getPress(), article.getPubDate()));
            }

            String prompt = String.format("""
                다음은 %s에 대한 뉴스 기사 목록입니다. 각 기사가 %s의 가격, 수급, 작황, 재배 등과 직접적으로 관련이 있는지 판단해주세요.
                가장 관련성 높은 3~5개의 기사만 선택해주세요.
                
                기사 목록:
                %s
                
                다음 형식으로만 답변해주세요:
                [기사 제목]: [관련 여부 (true/false)]
                
                예시:
                "감자 가격 상승 전망": true
                "감자 요리 레시피": false
                
                주의사항:
                - 가격, 수급, 작황, 재배 등과 직접적으로 관련된 기사만 true로 판단
                - 요리, 레시피, 일반 소식 등은 false로 판단
                - 반드시 기사 제목을 정확히 포함하여 답변
                - 가장 관련성 높은 3~5개의 기사만 선택
                """, cropName, cropName, articlesText.toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Map.of("parts", new Object[]{Map.of("text", prompt)}));
            requestBody.put("generationConfig", Map.of(
                "temperature", 0.1,
                "topK", 1,
                "topP", 0.1,
                "maxOutputTokens", 1024
            ));

            HttpPost request = new HttpPost("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), "UTF-8"));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String llmResponse = jsonResponse.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                
                String[] lines = llmResponse.split("\n");
                for (String line : lines) {
                    if (line.contains(": true")) {
                        String title = line.split(":")[0].trim();
                        articles.stream()
                            .filter(a -> a.getTitle().equals(title))
                            .findFirst()
                            .ifPresent(relevantArticles::add);
                    }
                }
            }
        } catch (Exception e) {
            log.error("LLM 관련성 확인 중 오류 발생: {}", e.getMessage(), e);
        }
        
        if (relevantArticles.size() < 3) {
            return articles.subList(0, Math.min(3, articles.size()));
        }
        
        return relevantArticles.size() > 5 ? 
            relevantArticles.subList(0, 5) : relevantArticles;
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

    public PricePredictionResponse predictPrice(String cropName) {
        // 뉴스 기사 가져오기
        List<NewsArticleDto> newsArticles = newsArticleService.getNewsArticles(cropName);
        log.info("가져온 뉴스 기사 목록:");
        newsArticles.forEach(article -> log.info("기사: {}", article));
        
        // 기사 정보를 문자열로 변환
        StringBuilder newsText = new StringBuilder();
        for (NewsArticleDto article : newsArticles) {
            newsText.append(String.format("제목: %s\n신문사: %s\n날짜: %s\n\n", 
                article.getTitle(), article.getPress(), article.getPubDate()));
        }
        
        return predictPrice(cropName, newsText.toString());
    }

    private PricePredictionResponse predictPrice(String cropName, String newsArticle) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String prompt = String.format("""
                다음 농작물의 시장 가격을 예측해주세요:
                농작물: %s
                관련 뉴스 기사: %s
                
                다음 형식으로만 답변해주세요. 다른 설명은 하지 마세요:
                    1. 예상 가격 수준 : [반드시 '비쌈', '보통', '쌈' 중 하나로만 답변]
                    2. 판단 근거 : [한 줄로 간단하게, '~해요'로 끝나는 부드러운 말투로 작성]
                3. 참고 기사 링크 : [해당 작물의 가격, 수급, 작황과 직접적으로 관련된 기사만 3개 이내로 선택]
                
                주의사항:
                    - 예상 가격 수준은 반드시 '비쌈', '보통', '쌈' 중 하나로만 답변해주세요.
                    - 판단이 불가능한 경우에도 '보통'으로 답변해주세요.
                    - 판단 근거는 반드시 '~해요'로 끝나는 부드러운 말투로 작성해주세요.
                - 참고 기사는 반드시 해당 작물의 가격, 수급, 작황과 직접적으로 관련된 기사만 선택해주세요.
                - 관련 없는 기사는 무시하고 관련 있는 기사만 선택해주세요.
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
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    String aiResponse = jsonResponse.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                    
                    PricePredictionResponse parsedResponse = parseAIResponse(aiResponse, cropName);
                    if (parsedResponse != null) {
                        return parsedResponse;
                    }
                    
                    log.warn("AI 응답 형식이 올바르지 않습니다. 재시도 중... (시도 {}/{})", attempt + 1, MAX_RETRIES);
                }
            } catch (IOException e) {
                log.error("가격 예측 중 오류 발생: {}", e.getMessage(), e);
            }
        }
        
        // 모든 시도가 실패한 경우 기본 응답 반환
        return PricePredictionResponse.builder()
                .reason("정보가 부족하여 정확한 예측이 어려워요")
                .priceLevel(1) // 기본값은 보통(1)
                .build();
    }

    private PricePredictionResponse parseAIResponse(String aiResponse, String cropName) {
        try {
            // 가격 수준 추출
            Pattern pricePattern = Pattern.compile("예상 가격 수준\\s*:\\s*(비쌈|보통|쌈)");
            Matcher priceMatcher = pricePattern.matcher(aiResponse);
            if (!priceMatcher.find()) {
                return null;
            }
            String priceLevel = priceMatcher.group(1);

            // 판단 근거 추출
            Pattern reasonPattern = Pattern.compile("판단 근거\\s*:\\s*([^\\n]+)");
            Matcher reasonMatcher = reasonPattern.matcher(aiResponse);
            if (!reasonMatcher.find()) {
                return null;
            }
            String reason = reasonMatcher.group(1).trim();

            // 가격 레벨 생성
            int priceLevelInt;
            switch (priceLevel) {
                case "비쌈":
                    priceLevelInt = 2;
                    break;
                case "쌈":
                    priceLevelInt = 0;
                    break;
                default:
                    priceLevelInt = 1;
            }

            return PricePredictionResponse.builder()
                    .reason(reason)
                    .priceLevel(priceLevelInt)
                    .build();
        } catch (Exception e) {
            log.error("AI 응답 파싱 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
} 