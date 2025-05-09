package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.repository.InformationRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j // 로깅을 위해 Lombok @Slf4j 추가
public class CsvImportService {

    private final InformationRepository informationRepository;

    @Transactional // 메서드 전체를 하나의 트랜잭션으로 묶음
    public void importInformationData() {
        ClassPathResource resource = new ClassPathResource("dataset.csv");
        int successfullySavedCount = 0;
        int failedCount = 0;

        // CSV 파일 인코딩은 UTF-8로 가정합니다. 다를 경우 수정 필요.
        try (InputStreamReader isr = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(isr)) {

            String[] nextLine;
            boolean isHeader = true; // 첫 번째 줄은 헤더로 간주

            while ((nextLine = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    if (nextLine.length > 0 && nextLine[0].trim().equalsIgnoreCase("itemcategorycode")) {
                        // 헤더가 예상과 일치하는지 간단히 확인 (선택적)
                        log.info("CSV 헤더 확인: {}", (Object) nextLine);
                    } else {
                        log.warn("CSV 헤더가 예상과 다를 수 있습니다: {}", (Object) nextLine);
                        // 헤더가 아니거나 예상치 못한 형식이라면, 첫 줄부터 데이터로 처리할지 결정 필요
                        // 여기서는 일단 헤더로 간주하고 건너뜁니다.
                    }
                    continue; 
                }
                
                // 빈 줄이나 내용이 거의 없는 줄 건너뛰기
                if (nextLine.length == 0 || (nextLine.length == 1 && nextLine[0].trim().isEmpty())) {
                    log.debug("빈 CSV 라인을 건너뜁니다.");
                    continue;
                }

                // CSV 컬럼 순서: itemcategorycode, itemcategoryname, itemcode, itemname, kindcode, kindname, unit, size
                if (nextLine.length < 8) {
                    log.warn("CSV 라인에 컬럼 수가 부족합니다 (8개 필요). 건너뜁니다: {}", (Object) nextLine);
                    failedCount++;
                    continue;
                }

                try {
                    String itemCategoryCode = nextLine[0].trim();
                    String itemCategoryName = nextLine[1].trim();
                    String itemCode = nextLine[2].trim();
                    String itemName = nextLine[3].trim();
                    String kindCode = nextLine[4].trim();
                    String kindName = nextLine[5].trim();
                    String unit = nextLine[6].trim();
                    String sizeStr = nextLine[7].trim();

                    int size = 0; // size의 기본값
                    if (!sizeStr.isEmpty()) {
                        try {
                            size = Integer.parseInt(sizeStr);
                        } catch (NumberFormatException e) {
                            log.warn("size 값 '{}'을(를) 숫자로 변환할 수 없어 기본값 0으로 설정. CSV 라인: {}", sizeStr, (Object)nextLine);
                        }
                    }
                    
                    // 필수 값 체크 (예: itemCategoryCode, itemCode, kindCode 등)
                    // 여기서는 간단히 로그만 남기고 진행하지만, 실제로는 더 엄격한 유효성 검사가 필요할 수 있습니다.
                    if (itemCategoryCode.isEmpty() || itemCode.isEmpty() || kindCode.isEmpty() || itemName.isEmpty()) {
                         log.warn("필수 정보가 누락된 CSV 라인을 건너뜁니다: {}", (Object)nextLine);
                         failedCount++;
                         continue;
                    }

                    Information information = Information.builder()
                            .itemCategoryCode(itemCategoryCode)
                            .itemCategoryName(itemCategoryName)
                            .itemCode(itemCode)
                            .itemName(itemName)
                            .kindCode(kindCode)
                            .kindName(kindName)
                            .unit(unit.isEmpty() ? null : unit) // unit이 비어있으면 null로 저장
                            .size(size)
                            .build();

                    log.info("저장 전 Information 이름 : {}", information.getKindName()); // itemName 등의 한글 필드가 정상인지 확인

                    informationRepository.save(information);
                    successfullySavedCount++;

                } catch (Exception e) { // 개별 행 처리 중 발생하는 예외
                    log.error("Information 엔티티 저장 중 오류 발생. CSV 라인: {}, 오류: {}", (Object)nextLine, e.getMessage());
                    failedCount++;
                }
            }
            log.info("CSV 데이터 가져오기 완료. 총 처리 라인: {}. 성공: {} 건, 실패: {} 건", (reader.getLinesRead() -1) , successfullySavedCount, failedCount);

        } catch (IOException | CsvValidationException e) {
            log.error("CSV 파일 처리 중 심각한 오류 발생: {}", e.getMessage(), e);
            // 이 경우 트랜잭션이 롤백될 가능성이 높음
            throw new RuntimeException("CSV 파일 처리 중 오류가 발생하여 작업을 중단합니다.", e);
        }
    }
} 