package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.kamis.KamisMonthlyApiBaseResponse;
import com.aolda.ojakgyo.dto.kamis.KamisPriceItemMonthlyDto;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice;
import com.aolda.ojakgyo.repository.InformationRepository;
import com.aolda.ojakgyo.repository.MonthlyPriceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KamisPriceCollectionService {

    private final InformationRepository informationRepository;
    private final MonthlyPriceRepository monthlyPriceRepository;
    private final ObjectMapper objectMapper; // ObjectMapper 주입 (Spring Boot가 자동 등록)

    @Value("${kamis.api.cert-key}")
    private String certKey;

    @Value("${kamis.api.cert-id}")
    private String certId;

    private static final String KAMIS_API_URL = "http://www.kamis.or.kr/service/price/xml.do";

    @Transactional
    public void collectMonthlyPriceDataForAllInformation() {
        List<Information> allInformation = informationRepository.findAll();
        log.info("총 {}개의 품목에 대해 월별 가격 데이터 수집을 시작합니다.", allInformation.size());

        for (Information info : allInformation) {
            collectMonthlyPriceDataForInformation(info);
            // API 호출 간에 약간의 지연을 두어 서버 부하를 줄이기
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        log.info("모든 품목에 대한 월별 가격 데이터 수집이 완료되었습니다.");
    }

    private void collectMonthlyPriceDataForInformation(Information information) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAMIS_API_URL)
                .queryParam("action", "monthlySalesList")
                .queryParam("p_yyyy", "2025") // 기준년도
                .queryParam("p_period", "6")  // 최근 6년치 (2020~2025). API가 이를 지원하는지 확인 필요. JSON 샘플은 2020년 포함.
                .queryParam("p_itemcategorycode", information.getItemCategoryCode())
                .queryParam("p_itemcode", information.getItemCode())
                .queryParam("p_kindcode", information.getKindCode())
                .queryParam("p_graderank", " ") // API 명세 확인 후 적절한 값으로 (샘플에서는 "2"였지만, 모든 품목에 유효한지 모름)
                .queryParam("p_countycode", " ") // API 명세 확인 후 적절한 값으로 (샘플에서는 "1101"이었지만, 모든 품목에 유효한지 모름)
                .queryParam("p_convert_kg_yn", "N")
                .queryParam("p_cert_key", certKey)
                .queryParam("p_cert_id", certId)
                .queryParam("p_returntype", "json");

        String apiUrl = uriBuilder.toUriString();
        log.debug("Requesting API URL: {}", apiUrl);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            if (jsonResponse == null) {
                log.warn("API 응답이 null입니다. 품목: {}/{}/{}", information.getItemCategoryCode(), information.getItemCode(), information.getKindCode());
                return;
            }

            KamisMonthlyApiBaseResponse apiResponse = objectMapper.readValue(jsonResponse, KamisMonthlyApiBaseResponse.class);

            if (apiResponse == null || !"000".equals(apiResponse.getErrorCode()) || apiResponse.getPrice() == null || apiResponse.getPrice().getItem() == null) {
                log.warn("API 에러 또는 데이터 없음. 품목: {}/{}/{}, 에러코드: {}, 응답: {}", 
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), 
                        apiResponse != null ? apiResponse.getErrorCode() : "N/A", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                return;
            }

            for (KamisPriceItemMonthlyDto priceItem : apiResponse.getPrice().getItem()) {
                Integer year = Integer.parseInt(priceItem.getYyyy());

                LocalDateTime now = LocalDateTime.now();
                int currentYear = now.getYear();
                int currentMonth = now.getMonthValue();

                if (year > currentYear || (year == currentYear && priceItem.getM1() == null)) {
                    log.warn("미래의 데이터가 포함되어 있습니다. 품목: {}/{}/{}, 년도: {}, 월: {}",
                            information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), year, priceItem.getM1());
                    continue;
                }
                String[] monthlyPrices = {
                    priceItem.getM1(), priceItem.getM2(), priceItem.getM3(), priceItem.getM4(), priceItem.getM5(), priceItem.getM6(),
                    priceItem.getM7(), priceItem.getM8(), priceItem.getM9(), priceItem.getM10(), priceItem.getM11(), priceItem.getM12()
                };

                for (int month = 1; month <= 12; month++) {
                    if (year == currentYear && month > currentMonth) {
                        break;
                    }

                    String priceStr = monthlyPrices[month - 1];
                    if (priceStr == null || "-".equals(priceStr.trim()) || priceStr.trim().isEmpty()) {
                        continue; // 가격 데이터 없음
                    }

                    try {
                        Integer price = Integer.parseInt(priceStr.replace(",", ""));

                        // 중복 확인
                        Optional<MonthlyPrice> existingPrice = monthlyPriceRepository.findByInformationAndPriceYearAndPriceMonth(information, year, month);
                        if (existingPrice.isEmpty()) {
                            MonthlyPrice newMonthlyPrice = MonthlyPrice.builder()
                                    .information(information)
                                    .priceYear(year)
                                    .priceMonth(month)
                                    .price(price)
                                    .build();
                            monthlyPriceRepository.save(newMonthlyPrice);
                        } else {
                            log.trace("이미 존재하는 월별 가격 데이터입니다: Information ID {}, Year {}, Month {}", information.getId(), year, month);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("가격 문자열 파싱 실패: '{}' for {}/{}/{} Year: {}, Month: {}", 
                                priceStr, information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), year, month);
                    }
                }
            }
        } catch (Exception e) {
            log.error("월별 가격 데이터 수집 중 오류 발생. 품목: {}/{}/{}, 오류: {}", 
                    information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), e.getMessage(), e);
        }
    }
} 