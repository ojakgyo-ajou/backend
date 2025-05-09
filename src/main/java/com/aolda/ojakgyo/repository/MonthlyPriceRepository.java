package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.MonthlyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MonthlyPriceRepository extends JpaRepository<MonthlyPrice, Long> {

    /**
     * itemCategoryCode, itemCode, kindCode가 일치하는 Information에 대한 월별 가격 데이터를
     * 연도와 월 오름차순으로 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @return 월별 가격 리스트
     */

    List<MonthlyPrice> findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAscPriceMonthAsc(
            String itemCategoryCode, String itemCode, String kindCode
    );
}