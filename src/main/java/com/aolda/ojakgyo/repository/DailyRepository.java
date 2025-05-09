package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.Daily;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DailyRepository extends JpaRepository<Daily, Long> {

    // 1일전 가격(dpr2)과 현재 가격(dpr1)의 차이 (dpr2 - dpr1)가 가장 큰 상품을 조회
    // (할인폭이 가장 큰 상품)
    @Query("SELECT d FROM Daily d WHERE d.dpr1 IS NOT NULL AND d.dpr2 IS NOT NULL " +
           "ORDER BY (CAST(d.dpr2 AS java.lang.Integer) - CAST(d.dpr1 AS java.lang.Integer)) DESC")
    List<Daily> findTopByOrderByDayPriceDifferenceDesc(Pageable pageable);
    
    // 1달전 가격(dpr3)과 현재 가격(dpr1)의 차이 (dpr3 - dpr1)가 가장 큰 상품을 조회
    // (할인폭이 가장 큰 상품)
    @Query("SELECT d FROM Daily d WHERE d.dpr1 IS NOT NULL AND d.dpr3 IS NOT NULL " +
           "ORDER BY (CAST(d.dpr3 AS java.lang.Integer) - CAST(d.dpr1 AS java.lang.Integer)) DESC")
    List<Daily> findTopByOrderByMonthPriceDifferenceDesc(Pageable pageable);

    // 1년전 가격(dpr4)와 현재 가격(dpr1)의 차이 (dpr4 - dpr1)가 가장 큰 상품을 조회
    // (할인폭이 가장 큰 상품)
    @Query("SELECT d FROM Daily d WHERE d.dpr1 IS NOT NULL AND d.dpr4 IS NOT NULL " +
           "ORDER BY (CAST(d.dpr4 AS java.lang.Integer) - CAST(d.dpr1 AS java.lang.Integer)) DESC")
    List<Daily> findTopByOrderByYearPriceDifferenceDesc(Pageable pageable);
}