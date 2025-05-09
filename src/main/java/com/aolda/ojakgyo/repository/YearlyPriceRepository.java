package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.YearlyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YearlyPriceRepository extends JpaRepository<YearlyPrice, Long> {

    /**
     * itemCategoryCode, itemCode, kindCode가 일치하는 Information에 대한 연도별 평균 가격 데이터를
     * 연도 오름차순으로 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @return 연도별 평균 가격 리스트
     */
    List<YearlyPrice> findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAsc(
            String itemCategoryCode, String itemCode, String kindCode
    );

    List<YearlyPrice> findByPriceYear(int year);
}