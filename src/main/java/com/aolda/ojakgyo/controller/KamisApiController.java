package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.kamis.DailyDto;
import com.aolda.ojakgyo.dto.KindPriceDto;
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice; 
import com.aolda.ojakgyo.entity.YearlyPrice;
import com.aolda.ojakgyo.service.InformationService;
import com.aolda.ojakgyo.service.kamis.KamisApiService;
import com.aolda.ojakgyo.dto.InformationDto;
import com.aolda.ojakgyo.dto.YearlyPriceDto;
import com.aolda.ojakgyo.dto.MonthlyPriceDto;
import com.aolda.ojakgyo.dto.DailyPriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/kamis") // 컨트롤러 기본 경로 설정
@RequiredArgsConstructor
public class KamisApiController {

    private final KamisApiService kamisApiService;
    private final InformationService informationService;
    
    // [메인 대시보드용] 금일 할인율 높은 상품 가져오기 
    @GetMapping("/dashboard") // 경로 명확화
    public ResponseEntity<Object> getDailyDiscountedProductsByPeriod(@RequestParam(value = "period", defaultValue = "day") String period) {
        switch (period.toLowerCase()) {
            
            // 전날에 비해 할인이 가장 많이 된 상품 20개까지 가져오기
            case "day":
                List<DailyDto> dailyDtoList = kamisApiService.getDailyTopDiscountedProducts();
                if (dailyDtoList == null || dailyDtoList.isEmpty()) {
                    return ResponseEntity.noContent().build(); // 204 No Content
                } 

                return ResponseEntity.ok(dailyDtoList);

            case "month":
                List<DailyDto> monthlyDtoList = kamisApiService.getMonthlyTopDiscountedProducts();
                if (monthlyDtoList == null || monthlyDtoList.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(monthlyDtoList);

            case "year":
                List<DailyDto> yearlyDtoList = kamisApiService.getYearlyTopDiscountedProducts();
                if (yearlyDtoList == null || yearlyDtoList.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(yearlyDtoList);

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid period specified.");
        }
    }

    /**
     * 그룹코드(itemCategoryCode)에 해당하는 품목들의 품종별(kindCode) 금일 가격 및 전일 대비 등락 정보를 제공합니다.
     * @param itemCategoryCode 조회할 그룹코드 (예: "100" - 식량작물)
     * @return 해당 그룹코드 내 품종별 가격 정보 리스트
     */
    @GetMapping("/price/itemcategory")
    public ResponseEntity<Object> getKindPricesByItemCategory(@RequestParam(value = "itemCategoryCode") String itemCategoryCode) {
        List<KindPriceDto> kindPrices = kamisApiService.getKindPricesByItemCategoryCode(itemCategoryCode);

        // 모든 품목을 가져왔음. 이제 품목 (쌀)에 대해서 모든 품종코드 (kindcode)에 대해서 금일 가격과 전일 대비 등락폭을 제공하기

        // 예시: 쌀의 품종코드에 대한 금일 가격과 전일 대비 등락폭을 가져오는 로직
        if (kindPrices.isEmpty()) {
            return ResponseEntity.noContent().build(); // 데이터가 없으면 204 No Content
        }
        return ResponseEntity.ok(kindPrices);
    }

    // DB에 존재하는 모든 상품 정보 가져오기 (이름 코드 정보)
    @GetMapping("/category")
    public ResponseEntity<List<InformationDto>> getKamisCategories() {
        List<Information> informationList = informationService.getAllInformation();
        if (informationList.isEmpty()) {
            return ResponseEntity.noContent().build(); // 데이터가 없을 경우 204 No Content 응답
        }
        // Information 리스트를 InformationDto 리스트로 변환
        List<InformationDto> informationDtoList = informationList.stream()
                .map(InformationDto::fromEntity) // InformationDto의 정적 메서드 fromEntity 사용
                .collect(Collectors.toList());
        return ResponseEntity.ok(informationDtoList); 
    }
    
    /**
     * mode(day, month, year)에 따라 일별/월별/연별 가격 리스트를 반환하는 통합 API
     * @param mode 조회 모드 (day, month, year)
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     */
    @GetMapping("/price")
    public ResponseEntity<?> getPriceByMode(
            @RequestParam(name = "mode", required = true) String mode,
            @RequestParam(name = "itemCategoryCode", required = true) String itemCategoryCode,
            @RequestParam(name = "itemCode", required = true) String itemCode,
            @RequestParam(name = "kindCode", required = true) String kindCode) {

        switch (mode.toLowerCase()) {
            case "day": {
                LocalDateTime now = LocalDateTime.now();
                int currentYear = now.getYear();
                int currentMonth = now.getMonthValue();
                List<DailyPrice> dailyPrices = kamisApiService.getDailyPrices(itemCategoryCode, itemCode, kindCode, currentYear, currentMonth);
                if (dailyPrices == null || dailyPrices.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }

                System.out.println("No data found.");

                List<DailyPriceDto> dailyPriceDtos = dailyPrices.stream().map(dp -> DailyPriceDto.builder()
                        .itemCategoryCode(dp.getInformation().getItemCategoryCode())
                        .itemCategoryName(dp.getInformation().getItemCategoryName())
                        .itemCode(dp.getInformation().getItemCode())
                        .itemName(dp.getInformation().getItemName())
                        .kindCode(dp.getInformation().getKindCode())
                        .kindName(dp.getInformation().getKindName())
                        .unit(dp.getInformation().getUnit())
                        .year(dp.getYear())
                        .month(dp.getMonth())
                        .day(dp.getDay())
                        .price(dp.getPrice())
                        .build()
                ).collect(Collectors.toList());
                return ResponseEntity.ok(dailyPriceDtos);
            }
            case "month": {
                YearMonth currentYm = YearMonth.now();
                YearMonth startRangeYm = currentYm.minusYears(1).plusMonths(1);
                int startYear = startRangeYm.getYear();
                int startMonth = startRangeYm.getMonthValue();
                int endYear = currentYm.getYear();
                int endMonth = currentYm.getMonthValue();
                List<MonthlyPrice> monthlyPrices = kamisApiService.getMonthlyPrices(
                        itemCategoryCode, itemCode, kindCode, startYear, startMonth, endYear, endMonth);
                if (monthlyPrices == null || monthlyPrices.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                List<MonthlyPriceDto> monthlyPriceDtos = monthlyPrices.stream().map(mp -> MonthlyPriceDto.builder()
                        .itemCategoryCode(mp.getInformation().getItemCategoryCode())
                        .itemCategoryName(mp.getInformation().getItemCategoryName())
                        .itemCode(mp.getInformation().getItemCode())
                        .itemName(mp.getInformation().getItemName())
                        .kindCode(mp.getInformation().getKindCode())
                        .kindName(mp.getInformation().getKindName())
                        .unit(mp.getInformation().getUnit())
                        .year(mp.getPriceYear())
                        .month(mp.getPriceMonth())
                        .price(mp.getPrice())
                        .build()
                ).collect(Collectors.toList());
                return ResponseEntity.ok(monthlyPriceDtos);
            }
            case "year": {
                // 최근 5개년치만 반환
                List<YearlyPrice> yearlyPrices = kamisApiService.getYearlyPrices(itemCategoryCode, itemCode, kindCode);
                if (yearlyPrices == null || yearlyPrices.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                List<YearlyPriceDto> yearlyPriceDtos = yearlyPrices.stream()
                        .sorted((a, b) -> b.getPriceYear() - a.getPriceYear()) // 최신 연도 우선 정렬
                        .limit(5)
                        .map(yp -> YearlyPriceDto.builder()
                                .itemCategoryCode(yp.getInformation().getItemCategoryCode())
                                .itemCategoryName(yp.getInformation().getItemCategoryName())
                                .itemCode(yp.getInformation().getItemCode())
                                .itemName(yp.getInformation().getItemName())
                                .kindCode(yp.getInformation().getKindCode())
                                .kindName(yp.getInformation().getKindName())
                                .unit(yp.getInformation().getUnit())
                                .year(yp.getPriceYear())
                                .averagePrice(yp.getAveragePrice())
                                .build()
                        ).collect(Collectors.toList());
                return ResponseEntity.ok(yearlyPriceDtos);
            }
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("mode 파라미터는 day, month, year 중 하나여야 합니다.");
        }
    }
}