package com.aolda.ojakgyo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private String title;      // 기사 제목
    private String press;      // 신문사
    private String pubDate;    // 기사 작성일
    private String link;       // 기사 링크 추가
} 