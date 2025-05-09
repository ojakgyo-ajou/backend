package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.kamis.DailyDto;
import com.aolda.ojakgyo.dto.KindPriceDto; // KindPriceDto 임포트
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information; // Information 임포트
import com.aolda.ojakgyo.entity.MonthlyPrice;
import com.aolda.ojakgyo.entity.YearlyPrice;
import com.aolda.ojakgyo.repository.DailyPriceRepository;
import com.aolda.ojakgyo.repository.MonthlyPriceRepository;
import com.aolda.ojakgyo.repository.YearlyPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // ArrayList 임포트
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class KamisApiService {

    @Value("${kamis.api.cert-key}")
    private String p_cert_key;

    @Value("${kamis.api.cert-id}")
    private String p_cert_id;

    private final DailyPriceRepository dailyPriceRepository;
    private final MonthlyPriceRepository monthlyPriceRepository;
    private final YearlyPriceRepository yearlyPriceRepository;

    public List<KindPriceDto> getKindPricesByItemCategoryCode(String itemCategoryCode) {
        return null;
    }

    /**
     * 특정 상품(itemCategoryCode, itemCode, kindCode 기준)의 월별 가격 데이터를 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @return 월별 가격 리스트 (연도, 월 오름차순 정렬)
     */
    public List<MonthlyPrice> getMonthlyPrices(String itemCategoryCode, String itemCode, String kindCode) {
        return monthlyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAscPriceMonthAsc(
                itemCategoryCode, itemCode, kindCode);
    }

    /**
     * 특정 상품(itemCategoryCode, itemCode, kindCode 기준)의 연평균 가격 데이터를 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @return 연평균 가격 리스트 (연도 오름차순 정렬)
     */
    public List<YearlyPrice> getYearlyPrices(String itemCategoryCode, String itemCode, String kindCode) {
        return yearlyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAsc(
                itemCategoryCode, itemCode, kindCode);
    }

    /**
     * 특정 품목에 대한 금일 가격 리스트를 반환함.
     *
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @return 해당 품목의 금일 가격 리스트
     */
    public List<DailyPrice> getDailyPrices(String itemCategoryCode, String itemCode, String kindCode) {
        return dailyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAscPriceMonthAscPriceDayAsc(
                itemCategoryCode, itemCode, kindCode);
    }
    
    /**
     * 전날에 비해 할인이 가장 많이 된 상품 20개까지 가져오기
     * @return 할인율 높은 상품 리스트
     */
    public List<DailyDto> getDailyTopDiscountedProducts() {
        List<DailyPrice> allDailyPrices = dailyPriceRepository.findAll(); 

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Map<Information, List<DailyPrice>> pricesByProduct = allDailyPrices.stream()
                .collect(Collectors.groupingBy(DailyPrice::getInformation));

        List<DailyDto> discountedProducts = new ArrayList<>();

        for (Map.Entry<Information, List<DailyPrice>> entry : pricesByProduct.entrySet()) {
            Information productInfo = entry.getKey();
            List<DailyPrice> prices = entry.getValue();

            Optional<DailyPrice> todayPriceOpt = prices.stream()
                    .filter(p -> p.getDate() != null && p.getDate().equals(today))
                    .findFirst();

            Optional<DailyPrice> yesterdayPriceOpt = prices.stream()
                    .filter(p -> p.getDate() != null && p.getDate().equals(yesterday))
                    .findFirst();

            if (todayPriceOpt.isPresent() && yesterdayPriceOpt.isPresent()) {
                DailyPrice todayPriceEntity = todayPriceOpt.get();
                DailyPrice yesterdayPriceEntity = yesterdayPriceOpt.get();

                if (yesterdayPriceEntity.getPrice() != null && yesterdayPriceEntity.getPrice() > 0 && todayPriceEntity.getPrice() != null) {
                    DailyDto dailyDto = new DailyDto(
                            productInfo.getItemName(),      // productName (품목명)
                            productInfo.getKindName(),      // itemName (품종명)
                            productInfo.getUnit(),          // unit
                            today.toString(),               // latestDate
                            todayPriceEntity.getPrice(),    // dpr1 (금일 가격)
                            yesterdayPriceEntity.getPrice() // dpr2 (전일 가격)
                    );

                    if (dailyDto.getPriceChangeRate() != null && dailyDto.getPriceChangeRate() < 0) {
                        discountedProducts.add(dailyDto);
                    }
                }
            }
        }
        
        discountedProducts.sort(Comparator.comparing(DailyDto::getPriceChangeRate, Comparator.nullsLast(Double::compareTo)));
        return discountedProducts.stream().limit(20).collect(Collectors.toList());
    }

    public List<DailyDto> getMonthlyTopDiscountedProducts() {
        // TODO: 월별 할인율 계산 로직 구현 (MonthlyPriceRepository 사용)
        // 전월 대비 할인율 계산
        return Collections.emptyList();
    }

    public List<DailyDto> getYearlyTopDiscountedProducts() {
        // TODO: 연도별 할인율 계산 로직 구현 (YearlyPriceRepository 사용)
        // 전년 대비 할인율 계산
        return Collections.emptyList();
    }
}