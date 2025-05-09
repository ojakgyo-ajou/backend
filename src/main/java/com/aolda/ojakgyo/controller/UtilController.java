package com.aolda.ojakgyo.controller; // 실제 패키지 경로

import com.aolda.ojakgyo.service.CsvImportService;
import com.aolda.ojakgyo.service.kamis.KamisPriceCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UtilController {

    private final CsvImportService csvImportService;
    private final KamisPriceCollectionService kamisPriceCollectionService;

    @PostMapping("/dataset")
    public ResponseEntity<String> importInformationCsv() {
        try {
            csvImportService.importInformationData();
            return ResponseEntity.ok("Information CSV 데이터 가져오기 성공");
        } catch (Exception e) {
            // 실제로는 더 구체적인 오류 응답 처리
            return ResponseEntity.status(500).body("Information CSV 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    @PostMapping("/price")
    public ResponseEntity<String> getKamisMonthlyData() {
        try {
            kamisPriceCollectionService.collectMonthlyPriceDataForAllInformation();
            return ResponseEntity.ok("월별 가격 데이터 수집 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("월별 가격 데이터 수집 실패: " + e.getMessage());
        }
    }

    @PostMapping("/daily-price")
    public ResponseEntity<String> getKamisDailyData() { 
        try {
            kamisPriceCollectionService.collectDailyPriceDataForAllInformationToday();
            return ResponseEntity.ok("오늘 날짜 일별 가격 데이터 수집 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("오늘 날짜 일별 가격 데이터 수집 실패: " + e.getMessage());
        }
    }
}