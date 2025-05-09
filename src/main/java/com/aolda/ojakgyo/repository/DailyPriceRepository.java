package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DailyPriceRepositoryCustom {
    List<DailyPrice> findByConditionsOrderByDateAsc(String categoryCode, String itemCode, String kindCode, int dateYear);
}

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {

    // 연도와 월을 통해서 모든 해당하는 일일 가격을 반환하기 - QueryDsl로 구현 (JPA 사용하지 않을것 )
    List<DailyPrice> findByConditionsOrderByDateAsc(String itemCategoryCode, String itemCode, String kindCode, int year, int month);

    List<DailyPrice> findByYearAndMonthAndDay(String year, String month, String day);
}