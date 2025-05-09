package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {

    /**
     * 특정 품목에 대한 금일 가격 리스트를 반환함.
     *
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @return 해당 품목의 금일 가격 리스트
     */
    List<DailyPrice> findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAscPriceMonthAscPriceDayAsc(String itemCategoryCode, String itemCode, String kindCode);

    Optional<DailyPrice> findByInformationAndDate(Information information, LocalDate date);
}
