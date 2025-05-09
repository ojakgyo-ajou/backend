package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    // 특정 작물의 최근 기사 조회 (최대 5개)
    @Query("SELECT n FROM NewsArticle n WHERE n.cropName = :cropName ORDER BY n.pubDate DESC LIMIT 5")
    List<NewsArticle> findTop5ByCropNameOrderByPubDateDesc(@Param("cropName") String cropName);
    
    // 특정 작물의 기사가 있는지 확인
    boolean existsByCropName(String cropName);
    
    // 특정 기간 이전의 기사 삭제
    @Query("DELETE FROM NewsArticle n WHERE n.createdAt < :date")
    void deleteByCreatedAtBefore(@Param("date") LocalDateTime date);
} 