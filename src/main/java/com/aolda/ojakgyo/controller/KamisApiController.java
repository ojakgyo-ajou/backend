package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.kamis.DailyDto;
import com.aolda.ojakgyo.dto.KindPriceDto;
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice; 
import com.aolda.ojakgyo.entity.YearlyPrice;
import com.aolda.ojakgyo.service.InformationService;
import com.aolda.ojakgyo.service.KamisApiService;
import com.aolda.ojakgyo.dto.InformationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
     * 특정 상품에 대한 월별 가격 리스트를 반환함.
     *
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @return 해당 상품의 월별 가격 리스트
     */
    @GetMapping("/price/monthly")
    public ResponseEntity<List<MonthlyPrice>> getMonthlyPrice(
            @RequestParam(required = true) String itemCategoryCode,
            @RequestParam(required = true) String itemCode,
            @RequestParam(required = true) String kindCode) {
        List<MonthlyPrice> monthlyPrices = kamisApiService.getMonthlyPrices(itemCategoryCode, itemCode, kindCode);
        if (monthlyPrices.isEmpty()) {
            return ResponseEntity.noContent().build(); // 데이터가 없으면 204 No Content
        }
        return ResponseEntity.ok(monthlyPrices);
    }

    /**
     * 특정 품목에 대한 금일 가격 리스트를 반환함.
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @return 해당 품목의 금일 가격 리스트
     */
    @GetMapping("/price/daily")
    public ResponseEntity<List<DailyPrice>> getDailyPrice(
            @RequestParam(required = true) String itemCategoryCode,
            @RequestParam(required = true) String itemCode,
            @RequestParam(required = true) String kindCode) {
        List<DailyPrice> dailyPrices = kamisApiService.getDailyPrices(itemCategoryCode, itemCode, kindCode);
        if (dailyPrices.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dailyPrices);
    }

    /**
     * 특정 상품에 대한 연평균 가격 리스트를 반환함.
     *
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @return 해당 품목의 금일 가격 리스트
     */
    @GetMapping("/price/yearly")
    public ResponseEntity<List<YearlyPrice>> getYearlyAveragePrice(
            @RequestParam(required = true) String itemCategoryCode,
            @RequestParam(required = true) String itemCode,
            @RequestParam(required = true) String kindCode) {
        List<YearlyPrice> yearlyPrices = kamisApiService.getYearlyPrices(itemCategoryCode, itemCode, kindCode);
        if (yearlyPrices.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(yearlyPrices);
    }
}