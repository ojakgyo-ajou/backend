package com.aolda.ojakgyo.service.kamis;

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Comparator;
import java.time.YearMonth;

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
     * 특정 상품(itemCategoryCode, itemCode, kindCode 기준)의 지정된 기간 내 월별 가격 데이터를 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @param startYear 시작 연도 
     * @param startMonth 시작 월
     * @param endYear 종료 연도
     * @param endMonth 종료 월
     * @return 월별 가격 리스트 (연도, 월 오름차순 정렬)
     */
    public List<MonthlyPrice> getMonthlyPrices(String itemCategoryCode, String itemCode, String kindCode, 
                                             int startYear, int startMonth, int endYear, int endMonth) {
        return monthlyPriceRepository.findPricesInDateRange(
                itemCategoryCode, itemCode, kindCode, startYear, startMonth, endYear, endMonth);
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
     * 특정 품목에 대한 "현재 월"의 일별 가격 리스트를 반환함.
     *
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @param year             조회할 연도
     * @param month            조회할 월
     * @return 해당 품목의 일별 가격 리스트
     */
    public List<DailyPrice> getDailyPrices(String itemCategoryCode, String itemCode, String kindCode, int year, int month) {

        return dailyPriceRepository.findByYearAndMonthAndDay(String.valueOf(year), String.format("%02d", month), String.format("%02d", LocalDate.now().getDayOfMonth()));
    }

    /**
     * 전날에 비해 할인이 가장 많이 된 상품 20개까지 가져오기
     * @return 할인율 높은 상품 리스트
     */
    public List<DailyDto> getDailyTopDiscountedProducts() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<DailyPrice> pricesForToday = dailyPriceRepository.findByYearAndMonthAndDay(String.valueOf(today.getYear()), String.format("%02d", today.getMonthValue()), String.format("%02d", today.getDayOfMonth()));
        List<DailyPrice> pricesForYesterday = dailyPriceRepository.findByYearAndMonthAndDay(String.valueOf(yesterday.getYear()), String.format("%02d", yesterday.getMonthValue()), String.format("%02d", yesterday.getDayOfMonth()));

        Map<Information, DailyPrice> todayPriceMap = pricesForToday.stream()
                .collect(Collectors.toMap(DailyPrice::getInformation, dp -> dp, (dp1, dp2) -> dp1));

        Map<Information, DailyPrice> yesterdayPriceMap = pricesForYesterday.stream()
                .collect(Collectors.toMap(DailyPrice::getInformation, dp -> dp, (dp1, dp2) -> dp1));

        List<DailyDto> discountedProducts = new ArrayList<>();

        for (Map.Entry<Information, DailyPrice> yesterdayEntry : yesterdayPriceMap.entrySet()) {
            Information productInfo = yesterdayEntry.getKey();
            DailyPrice yesterdayPriceEntity = yesterdayEntry.getValue();
            DailyPrice todayPriceEntity = todayPriceMap.get(productInfo);

            if (todayPriceEntity != null && yesterdayPriceEntity.getPrice() != null && todayPriceEntity.getPrice() != null && yesterdayPriceEntity.getPrice() > 0) {
                DailyDto dailyDto = new DailyDto(
                        productInfo.getItemName(),
                        productInfo.getKindName(), 
                        productInfo.getUnit(),
                        today.toString(),
                        todayPriceEntity.getPrice(),
                        yesterdayPriceEntity.getPrice()
                );

                if (dailyDto.getPriceChangeRate() != null && dailyDto.getPriceChangeRate() < 0) {
                    discountedProducts.add(dailyDto);
                }
            }
        }

        discountedProducts.sort(Comparator.comparing(DailyDto::getPriceChangeRate, Comparator.nullsLast(Double::compareTo)));

        return discountedProducts.stream().limit(20).collect(Collectors.toList());
    }
    
    public List<DailyDto> getMonthlyTopDiscountedProducts() {
        LocalDate currentDate = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(currentDate);
        YearMonth previousYearMonth = currentYearMonth.minusMonths(1);

        List<MonthlyPrice> currentPrices = monthlyPriceRepository.findByPriceYearAndPriceMonth(currentYearMonth.getYear(), currentYearMonth.getMonthValue());
        List<MonthlyPrice> previousPrices = monthlyPriceRepository.findByPriceYearAndPriceMonth(previousYearMonth.getYear(), previousYearMonth.getMonthValue());

        Map<Information, MonthlyPrice> currentPriceMap = currentPrices.stream()
                .collect(Collectors.toMap(MonthlyPrice::getInformation, mp -> mp, (mp1, mp2) -> mp1));
        Map<Information, MonthlyPrice> previousPriceMap = previousPrices.stream()
                .collect(Collectors.toMap(MonthlyPrice::getInformation, mp -> mp, (mp1, mp2) -> mp1));

        List<DailyDto> discountedProducts = new ArrayList<>();

        for (Map.Entry<Information, MonthlyPrice> prevEntry : previousPriceMap.entrySet()) {
            Information productInfo = prevEntry.getKey();
            MonthlyPrice prevMonthPriceEntity = prevEntry.getValue();
            MonthlyPrice currentMonthPriceEntity = currentPriceMap.get(productInfo);

            if (currentMonthPriceEntity != null && prevMonthPriceEntity.getPrice() != null && currentMonthPriceEntity.getPrice() != null && prevMonthPriceEntity.getPrice() > 0) {
                String currentMonthStr = String.format("%d-%02d", currentYearMonth.getYear(), currentYearMonth.getMonthValue());
                DailyDto monthlyDto = new DailyDto(
                        productInfo.getItemName(),
                        productInfo.getKindName(),
                        productInfo.getUnit(),
                        currentMonthStr, // 현재 연월 정보
                        currentMonthPriceEntity.getPrice(), // 현재 월 가격
                        prevMonthPriceEntity.getPrice() // 이전 월 가격
                );

                if (monthlyDto.getPriceChangeRate() != null && monthlyDto.getPriceChangeRate() < 0) {
                    discountedProducts.add(monthlyDto);
                }
            }
        }

        discountedProducts.sort(Comparator.comparing(DailyDto::getPriceChangeRate, Comparator.nullsLast(Double::compareTo)));

        return discountedProducts.stream().limit(20).collect(Collectors.toList());
    }
    
    public List<DailyDto> getYearlyTopDiscountedProducts() {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int previousYear = currentYear - 1;

        List<YearlyPrice> currentPrices = yearlyPriceRepository.findByPriceYear(currentYear);
        List<YearlyPrice> previousPrices = yearlyPriceRepository.findByPriceYear(previousYear);

        Map<Information, YearlyPrice> currentPriceMap = currentPrices.stream()
                .collect(Collectors.toMap(YearlyPrice::getInformation, yp -> yp, (yp1, yp2) -> yp1));
        Map<Information, YearlyPrice> previousPriceMap = previousPrices.stream()
                .collect(Collectors.toMap(YearlyPrice::getInformation, yp -> yp, (yp1, yp2) -> yp1));

        List<DailyDto> discountedProducts = new ArrayList<>();

        for (Map.Entry<Information, YearlyPrice> prevEntry : previousPriceMap.entrySet()) {
            Information productInfo = prevEntry.getKey();
            YearlyPrice prevYearPriceEntity = prevEntry.getValue();
            YearlyPrice currentYearPriceEntity = currentPriceMap.get(productInfo);

            if (currentYearPriceEntity != null && prevYearPriceEntity.getAveragePrice() != null && currentYearPriceEntity.getAveragePrice() != null && prevYearPriceEntity.getAveragePrice() > 0) {
                DailyDto yearlyDto = new DailyDto(
                        productInfo.getItemName(),
                        productInfo.getKindName(),
                        productInfo.getUnit(),
                        String.valueOf(currentYear), // 현재 연도 정보
                        currentYearPriceEntity.getAveragePrice(), // 현재 연도 가격
                        prevYearPriceEntity.getAveragePrice() // 이전 연도 가격
                );

                if (yearlyDto.getPriceChangeRate() != null && yearlyDto.getPriceChangeRate() < 0) {
                    discountedProducts.add(yearlyDto);
                }
            }
        }
        
        discountedProducts.sort(Comparator.comparing(DailyDto::getPriceChangeRate, Comparator.nullsLast(Double::compareTo)));
        return discountedProducts.stream().limit(20).collect(Collectors.toList());
    }
}