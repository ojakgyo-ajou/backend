package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

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

    Optional<MonthlyPrice> findByInformationAndPriceYearAndPriceMonth(Information information, Integer year, Integer month);

    @Query("SELECT mp FROM MonthlyPrice mp " +
           "WHERE mp.information.itemCategoryCode = :itemCategoryCode " +
           "AND mp.information.itemCode = :itemCode " +
           "AND mp.information.kindCode = :kindCode " +
           "AND mp.priceYear = :startYear " +
           "AND mp.priceMonth >= :startMonth " +
           "AND mp.priceYear = :endYear " +
           "AND mp.priceMonth <= :endMonth")
    List<MonthlyPrice> findPricesInDateRange(
            @Param("itemCategoryCode") String itemCategoryCode,
            @Param("itemCode") String itemCode,
            @Param("kindCode") String kindCode,
            @Param("startYear") int startYear,
            @Param("startMonth") int startMonth,
            @Param("endYear") int endYear,
            @Param("endMonth") int endMonth
    );

    List<MonthlyPrice> findByPriceYearAndPriceMonth(int year, int month);
}