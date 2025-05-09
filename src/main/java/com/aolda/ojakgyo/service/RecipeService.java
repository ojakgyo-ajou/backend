package com.aolda.ojakgyo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecipeService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getRecipe(String cropName) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String prompt = String.format("""
                다음 농작물을 이용한 맛있는 레시피를 알려주세요: %s
                레시피는 다음 형식으로 작성해주세요:
                1. 재료
                2. 조리 방법
                3. 조리 시간
                4. 난이도
                5. 팁
                """, cropName);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Map.of("parts", new Object[]{Map.of("text", prompt)}));
            requestBody.put("generationConfig", Map.of(
                "temperature", 0.7,
                "topK", 40,
                "topP", 0.95,
                "maxOutputTokens", 1024
            ));

            HttpPost request = new HttpPost("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), "UTF-8"));

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println(responseBody);
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                return jsonResponse.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("레시피 생성 중 오류가 발생했습니다.", e);
        }
    }
} 