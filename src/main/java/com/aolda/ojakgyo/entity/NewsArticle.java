package com.aolda.ojakgyo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_articles")
public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cropName;    // 관련 작물명

    @Column(nullable = false)
    private String title;       // 기사 제목

    @Column(nullable = false)
    private String press;       // 신문사

    @Column(nullable = false)
    private String pubDate;     // 기사 작성일

    @Column(nullable = false)
    private String link;        // 기사 링크

    @CreationTimestamp
    private LocalDateTime createdAt;    // DB 저장 시간

    @UpdateTimestamp
    private LocalDateTime updatedAt;    // DB 수정 시간
} 