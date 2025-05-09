package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.DailyDto;
import com.aolda.ojakgyo.service.KamisApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class KamisApiController {

    private final KamisApiService kamisApiService;

    /**
     * 농산물 가격 정보를 기간별로 조회합니다.
     * @param period 조회 기간 ("day", "month", "year"). 기본값 "day".
     * @return 기간별 할인 품목 정보 (문자열 형태, 추후 JSON 파싱 필요)
     */
    @GetMapping("/api/kamis")
    public ResponseEntity<Object> getKamisApi(@RequestParam(value = "period", defaultValue = "day", required = true) String period) {
        switch (period.toLowerCase()) {

            case "day":
                List<DailyDto> dtoList = kamisApiService.getDailyTop20DiscountedProducts()
                if(dtoList.isEmpty()) return ResponseEntity.notFound().build();
                return ResponseEntity.ok(dtoList);

            case "month":
                // 저번 월 대비 가장 많이 할인된 품목 조회
                return ResponseEntity.ok(null);

            case "year":
                // 저번 년도 대비 가장 많이 할인된 품목 조회
                return ResponseEntity.ok(null);

            default:
                return ResponseEntity.notFound().build();
        }
    }

    // 우리 DB에 있는 카테고리 리스트를 반환 하는 API. (무슨 품목들이 있는지 ㅇㅇ)
    @GetMapping("/api/kamis/category")


    // 품목 별로 그러면 월별 데이터 모두 가지고 있어야 되고,



    // 특정 농산물에 대한 월별 가격, 전일 대비 상승률을 배열로 전달하는 API
}