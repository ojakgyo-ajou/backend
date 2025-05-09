package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.DailyDto;
import com.aolda.ojakgyo.dto.KindPriceDto;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice; // MonthlyPrice 임포트
import com.aolda.ojakgyo.entity.YearlyPrice;
import com.aolda.ojakgyo.service.InformationService;
import com.aolda.ojakgyo.service.KamisApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // PathVariable 임포트 및 RequestMapping 추가

import java.util.List;

@RestController
@RequestMapping("/api/kamis") // 컨트롤러 기본 경로 설정
@RequiredArgsConstructor
public class KamisApiController {

    private final KamisApiService kamisApiService;

    private final InformationService informationService;

    // [메인 대시보드용] 할인율 높은 상품 가져오기
    @GetMapping("/discounted-products") // 경로 명확화
    public ResponseEntity<Object> getDiscountedProductsByPeriod(@RequestParam(value = "period", defaultValue = "day") String period) {
        switch (period.toLowerCase()) {
            case "day":
                List<DailyDto> dailyDtoList = null; // 만드는중
                if (dailyDtoList.isEmpty()) {
                    return ResponseEntity.noContent().build(); // 204 No Content
                }
                return ResponseEntity.ok(dailyDtoList);

            case "month":
                // kamisApiService.getMonthlyTopDiscountedProduct() 등의 결과를 반환
                // 현재 Optional<Daily>를 반환하므로, 필요시 DTO로 변환하거나 로직 수정
                return ResponseEntity.ok(null);

            case "year":
                return ResponseEntity.ok(null);

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid period specified.");
        }
    }

    /**
     * 그룹코드(itemCategoryCode)에 해당하는 품목들의 품종별(kindCode) 금일 가격 및 전일 대비 등락 정보를 제공합니다.
     * @param itemCategoryCode 조회할 그룹코드 (예: "100" - 식량작물)
     * @return 해당 그룹코드 내 품종별 가격 정보 리스트
     */
    @GetMapping("/price/itemcategory/{itemCategoryCode}")
    public ResponseEntity<Object> getKindPricesByItemCategory(@PathVariable String itemCategoryCode) {
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
    public ResponseEntity<Object> getKamisCategories(@RequestParam(value = "period", defaultValue = "day", required = false) String period) {

        List<Information> informationList = informationService.getAllInformation();
        if (informationList.isEmpty()) {
            return ResponseEntity.noContent().build(); // 데이터가 없을 경우 204 No Content 응답
        }
        return ResponseEntity.ok(informationList);
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
     *
     * @param itemCategoryCode 품목 카테고리 코드
     * @param itemCode         품목 코드
     * @param kindCode         품종 코드
     * @return 해당 품목의 금일 가격 리스트
     */

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