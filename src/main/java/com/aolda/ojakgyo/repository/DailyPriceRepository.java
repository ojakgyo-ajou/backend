package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DailyPriceRepositoryCustom {
    List<DailyPrice> findByYearAndMonthOrderByDateAsc(String itemCategoryCode, String itemCode, String kindCode, int year, int month);
}

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long>, DailyPriceRepositoryCustom {
    // findByYearAndMonthOrderByDateAsc는 QueryDsl 구현체를 통해 처리됨
    List<DailyPrice> findByYearAndMonthAndDay(String year, String month, String day);
}