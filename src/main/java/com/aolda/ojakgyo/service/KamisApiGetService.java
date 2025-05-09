package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.kamis.KamisApiResponse;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.repository.DailyPriceRepository;
import com.aolda.ojakgyo.repository.InformationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class KamisApiGetService {

    private final InformationRepository informationRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 ObjectMapper 주입

    // kamis api로부터 데이터를 가져오는 로직들
    public void savePriceDataFromJson(String jsonResponse) {
        try {
            KamisApiResponse apiResponse = objectMapper.readValue(jsonResponse, KamisApiResponse.class);

            if (apiResponse == null || apiResponse.getData() == null || apiResponse.getData().getItem() == null) {
                log.warn("API 응답 데이터가 비어있습니다.");
                return;
            }

            // 에러 코드가 "000" (성공)이 아니면 처리 중단
            if (!"000".equals(apiResponse.getData().getErrorCode())) {
                log.error("API 에러 발생: 코드 = {}, 응답 전문 = {}", apiResponse.getData().getErrorCode(), jsonResponse);
                // 필요하다면 사용자 정의 예외를 던지거나 다른 에러 처리를 할 수 있습니다.
                // 예: throw new ApiException("KAMIS API error: " + apiResponse.getData().getErrorCode());
                return;
            }

            KamisApiResponse.ConditionItem condition = apiResponse.getCondition().getItem();
            List<KamisApiResponse.PriceItem> priceItems = apiResponse.getData().getItem();

            // Information 엔티티를 위한 itemName과 kindName 추출
            // "평균"이나 "평년"이 아닌 첫 번째 데이터에서 추출
            final String[] actualItemName = {null};
            final String[] actualKindName = {null};
            for (KamisApiResponse.PriceItem item : priceItems) {
                if (item.getItemname() != null && !item.getItemname().isEmpty() &&
                        !"평균".equals(item.getCountyname()) && !"평년".equals(item.getCountyname())) {
                    actualItemName[0] = item.getItemname();
                    actualKindName[0] = item.getKindname();
                    break;
                }
            }
            
            if (actualItemName[0] == null) {
                log.warn("실제 품목명(itemName)을 API 응답에서 찾을 수 없습니다. (p_itemcode: {})", condition.getPItemcode());
            }

            // Information 엔티티 찾기 또는 생성
            // itemCategoryCode, itemCode, kindCode를 기준으로 Information 객체를 관리합니다.
            Information information = informationRepository.findByItemCategoryCodeAndItemCodeAndKindCode(
                    condition.getPItemcategorycode(),
                    condition.getPItemcode(),
                    condition.getPKindcode()
            ).orElseGet(() -> {
                Information newInfo = Information.builder()
                        .itemCategoryCode(condition.getPItemcategorycode())
                        .itemCode(condition.getPItemcode())
                        .kindCode(condition.getPKindcode())
                        .itemName(actualItemName[0]) // 추출한 실제 품목명 사용
                        .kindName(actualKindName[0]) // 추출한 실제 품종명 사용
                        .unit("Y".equalsIgnoreCase(condition.getPConvertKgYn()) ? "kg" : null) // kg 단위 변환 여부
                        .size(1) // p_convert_kg_yn=Y 이면 가격은 1kg 기준
                        .build();
                return informationRepository.save(newInfo);
            });

            // 만약 기존 Information 엔티티가 존재하고, itemName이나 kindName이 null이었는데
            // 이번 API 호출에서 값을 가져왔다면 업데이트 해줄 수 있습니다.
            if (information.getId() != null && (
                    (information.getItemName() == null && actualItemName[0] != null) ||
                            (information.getKindName() == null && actualKindName[0] != null)
            )) {
                boolean updated = false;
                if (information.getItemName() == null && actualItemName[0] != null) {
                    // Information 엔티티에 setter가 없으므로 builder를 다시 사용하거나 setter 추가 필요
                    // 여기서는 간단히 필드값을 채우고 저장하는 형태로 가정 (엔티티에 setter가 있다고 가정)
                    // information.setItemName(actualItemName[0]); // Setter가 있다면 이렇게
                    updated = true;
                }
                if (information.getKindName() == null && actualKindName[0] != null) {
                    // information.setKindName(actualKindName[0]); // Setter가 있다면 이렇게
                    updated = true;
                }
                if (updated) {
                    // Setter가 없다면, 새로운 빌더로 객체를 만들고 ID를 유지하는 방식은 복잡합니다.
                    // 가장 간단한 방법은 Information 엔티티에 필요한 setter를 추가하는 것입니다.
                    // 또는 변경 감지를 위해 해당 필드를 업데이트하는 메소드를 Information 엔티티에 추가합니다.
                    // 여기서는 간단히 로그만 남기고, 필요시 Information 엔티티 수정을 권장합니다.
                    log.info("Information ID {}의 itemName 또는 kindName 업데이트 필요 (현재는 자동 업데이트 안됨)", information.getId());
                    // information = informationRepository.save(information); // setter로 변경 후 저장
                }
            }


            List<DailyPrice> dailyPricesToSave = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");

            for (KamisApiResponse.PriceItem itemDto : priceItems) {
                // "평균" 또는 "평년" 데이터는 실제 품목 가격이 아니므로 건너뜁니다.
                // 또한, itemname이 null인 경우도 일반적인 품목 데이터가 아닐 가능성이 높습니다. (평균/평년 데이터에 해당)
                if ("평균".equals(itemDto.getCountyname()) || "평년".equals(itemDto.getCountyname()) || itemDto.getItemname() == null) {
                    continue;
                }

                // 날짜 파싱 (yyyy와 regday 결합)
                // regday가 "MM/dd" 형식이므로, 연도는 yyyy에서 가져옵니다.
                String monthDayStr = itemDto.getRegday();
                int year = Integer.parseInt(itemDto.getYyyy());

                String[] mdParts = monthDayStr.split("/");
                int month = Integer.parseInt(mdParts[0]);
                int day = Integer.parseInt(mdParts[1]);
                LocalDate date = LocalDate.of(year, month, day);

                // 가격 파싱 (쉼표 제거 후 Integer로 변환)
                // 가격이 "-" 인 경우 (데이터 없음) 처리
                if ("-".equals(itemDto.getPrice())) {
                    log.info("가격 정보 없음 (품목: {}, 날짜: {}), 건너뜁니다.", itemDto.getItemname(), date);
                    continue;
                }
                Integer price = Integer.parseInt(itemDto.getPrice().replace(",", ""));

                // 중복 저장 방지: 해당 Information과 날짜로 이미 데이터가 있는지 확인
                Optional<DailyPrice> existingDailyPrice = dailyPriceRepository.findByInformationAndDate(information, date);
                if (existingDailyPrice.isPresent()) {
                    // 이미 데이터가 있다면 업데이트하거나 건너뛸 수 있습니다. 여기서는 건너뜁니다.
                    log.debug("이미 존재하는 가격 데이터입니다: Information ID {}, Date {}", information.getId(), date);
                    continue;
                }

                DailyPrice dailyPrice = DailyPrice.builder()
                        .information(information)
                        .date(date)
                        .price(price)
                        .build();
                dailyPricesToSave.add(dailyPrice);
            }

            if (!dailyPricesToSave.isEmpty()) {
                dailyPriceRepository.saveAll(dailyPricesToSave);
                log.info("{}개의 DailyPrice 데이터 저장 완료. Information ID: {}", dailyPricesToSave.size(), information.getId());
            } else {
                log.info("저장할 새로운 DailyPrice 데이터가 없습니다. Information ID: {}", information.getId());
            }

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 에러 발생: {}", e.getMessage(), e);
            // throw new RuntimeException("JSON 파싱 에러", e); // 필요에 따라 예외를 다시 던질 수 있음
        } catch (NumberFormatException e) {
            log.error("숫자 변환 중 에러 발생 (가격 또는 날짜): {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("KAMIS 가격 데이터 저장 중 알 수 없는 에러 발생: {}", e.getMessage(), e);
        }
    }


}
