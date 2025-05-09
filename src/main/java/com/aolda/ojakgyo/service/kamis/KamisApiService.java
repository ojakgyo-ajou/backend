package com.aolda.ojakgyo.service.kamis;

import com.aolda.ojakgyo.dto.KamisDailyCountyListResponse;
import com.aolda.ojakgyo.dto.kamis.DailyDto;
import com.aolda.ojakgyo.dto.KindPriceDto; // KindPriceDto 임포트
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information; // Information 임포트
import com.aolda.ojakgyo.entity.MonthlyPrice;
import com.aolda.ojakgyo.entity.YearlyPrice;
import com.aolda.ojakgyo.repository.DailyPriceRepository;
import com.aolda.ojakgyo.repository.InformationRepository; // InformationRepository 임포트 추가
import com.aolda.ojakgyo.repository.MonthlyPriceRepository;
import com.aolda.ojakgyo.repository.YearlyPriceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 어노테이션 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j // Lombok Slf4j 어노테이션 추가
public class KamisApiService {

    @Value("${kamis.api.cert-key}")
    private String p_cert_key;

    @Value("${kamis.api.cert-id}")
    private String p_cert_id;

    private static final String KAMIS_API_BASE_URL = "https://www.kamis.or.kr"; // HTTPS 사용 권장

    private final RestTemplate restTemplate; // RestTemplate 빈 주입
    private final ObjectMapper objectMapper; // ObjectMapper 빈 주입
    private final MonthlyPriceRepository monthlyPriceRepository;
    private final YearlyPriceRepository yearlyPriceRepository;
    private final InformationRepository informationRepository;

    public List<DailyDto> getDailyTopDiscountedProducts() {
        String apiUrl = UriComponentsBuilder.fromHttpUrl(KAMIS_API_BASE_URL + "/service/price/xml.do")
                .queryParam("action", "dailyCountyList")
                .queryParam("p_cert_key", p_cert_key)
                .queryParam("p_cert_id", p_cert_id)
                .queryParam("p_returntype", "json")
                .queryParam("p_countycode", "1101") // 서울 지역 고정
                .encode() // URL 인코딩
                .toUriString();

        log.info("Requesting KAMIS Daily County List API: {}", apiUrl); // 로그 활성화
        List<DailyDto> dailyDtoList = new ArrayList<>();

        try {
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.warn("KAMIS API 응답이 비어있습니다. URL: {}", apiUrl); // 로그 활성화 및 URL 추가
                return Collections.emptyList();
            }

            if (jsonResponse.trim().startsWith("<")) {
                log.warn("KAMIS API 응답이 HTML입니다. URL: {}, 응답 일부: {}", apiUrl, jsonResponse.substring(0, Math.min(jsonResponse.length(), 200))); // 로그 활성화 및 URL, 응답 일부 추가
                return Collections.emptyList();
            }

            log.debug("KAMIS API Response: {}", jsonResponse); // 로그 활성화

            KamisDailyCountyListResponse apiResponse = objectMapper.readValue(jsonResponse, KamisDailyCountyListResponse.class);

            if (apiResponse == null || !"000".equals(apiResponse.getErrorCode()) || apiResponse.getPrice() == null) {
                log.warn("KAMIS API 에러 또는 데이터 없음. ErrorCode: {}, URL: {}", apiResponse != null ? apiResponse.getErrorCode() : "N/A", apiUrl); // 로그 활성화
                return Collections.emptyList();
            }

            for (KamisDailyCountyListResponse.PriceDetailItem item : apiResponse.getPrice()) {
                try {
                    if (item.getProductClsName() == null || !item.getProductClsName().contains("소매")) {
                        log.trace("소매 상품이 아님: {}", item.getProductName());
                        continue;
                    }

                    if (item.getDpr1() == null || item.getDpr1().isEmpty() || item.getDpr1().equals("[]") || "-".equals(item.getDpr1()) ||
                            item.getDpr2() == null || item.getDpr2().isEmpty() || item.getDpr2().equals("[]") || "-".equals(item.getDpr2()) ||
                            item.getValue() == null || item.getValue().isEmpty() || item.getValue().equals("[]") || "-".equals(item.getValue()) ||
                            item.getDirection() == null || item.getDirection().isEmpty()) {
                        log.debug("가격 정보 누락 또는 유효하지 않음: {}", item.getProductName()); // 로그 활성화
                        continue;
                    }

                    int yesterdayPriceNum = Integer.parseInt(item.getDpr2().replace(",", ""));
                    if (yesterdayPriceNum == 0) {
                        log.debug("전일 가격이 0이므로 할인율 계산 불가: {}", item.getProductName()); // 로그 활성화
                        continue;
                    }

                    if (!"0".equals(item.getDirection())) { // 가격 하락 상품만 대상
                        log.trace("가격 하락 상품이 아님 (direction != 0): {}", item.getProductName());
                        continue;
                    }

                    int todayPriceNum = Integer.parseInt(item.getDpr1().replace(",", ""));
                    double priceChangeRateValue = Double.parseDouble(item.getValue());
                    // API에서 direction이 "0" (하락)일 때 value는 보통 양수로 등락폭을 나타냅니다.
                    // 할인율을 음수로 표현하기 위해 -1을 곱합니다.
                    double actualPriceChangeRate = -priceChangeRateValue;

                    DailyDto dto = DailyDto.builder()
                            .productName(item.getProductName())
                            .unit(item.getUnit())
                            .latestDate(item.getLastestDay())
                            .todayPrice(todayPriceNum)
                            .yesterdayPrice(yesterdayPriceNum)
                            .priceChangeRate(actualPriceChangeRate)
                            .priceDirection(todayPriceNum == yesterdayPriceNum ? 2 : todayPriceNum < yesterdayPriceNum ? 0 : 1)
                            .build();
                    dailyDtoList.add(dto);
                    log.trace("DailyDto 추가됨: {}", dto);

                } catch (NumberFormatException e) {
                    log.warn("가격 또는 등락율 파싱 오류. Item: {}, dpr1: {}, dpr2: {}, value: {}, Error: {}", item.getProductName(), item.getDpr1(), item.getDpr2(), item.getValue(), e.getMessage()); // 로그 활성화
                } catch (Exception e) {
                    log.warn("DailyDto 변환 중 예외 발생. Item: {}, Error: {}", item.getProductName(), e.getMessage(), e); // 로그 활성화
                }
            }

            // 할인율(음수) 기준 오름차순 정렬 (가장 많이 하락한 상품이 먼저 오도록)
            dailyDtoList.sort(Comparator.comparingDouble(DailyDto::getPriceChangeRate));
            log.info("총 {}개의 할인 상품 DTO 생성 및 정렬 완료.", dailyDtoList.size());

        } catch (Exception e) {
            log.error("KAMIS API 호출 또는 파싱 중 오류 발생 (getDailyTopDiscountedProducts). URL: {}", apiUrl, e); // 로그 활성화
            return Collections.emptyList();
        }

        return dailyDtoList;
    }

    public List<KindPriceDto> getKindPricesByItemCategoryCode(String itemCategoryCode) {
        List<KindPriceDto> kindPrices = new ArrayList<>();
        List<Information> informations = informationRepository.findByItemCategoryCode(itemCategoryCode);
        if (informations.isEmpty()) {
            log.info("itemCategoryCode '{}'에 해당하는 Information 정보 없음.", itemCategoryCode);
            return Collections.emptyList();
        }

        String apiUrl = UriComponentsBuilder.fromHttpUrl(KAMIS_API_BASE_URL + "/service/price/xml.do")
                .queryParam("action", "dailyPriceByCategoryList")
                .queryParam("p_product_cls_code", "01") // 소매가격
                .queryParam("p_itemcategorycode", itemCategoryCode)
                .queryParam("p_cert_key", p_cert_key)
                .queryParam("p_cert_id", p_cert_id)
                .queryParam("p_returntype", "json")
                .encode()
                .toUriString();
        log.info("Requesting KAMIS Daily Price By Category List API: {}", apiUrl);

        try {
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.warn("KAMIS API 응답이 비어있습니다 (getKindPricesByItemCategoryCode). URL: {}", apiUrl);
                return Collections.emptyList();
            }
            if (jsonResponse.trim().startsWith("<")) {
                log.warn("KAMIS API 응답이 HTML입니다 (getKindPricesByItemCategoryCode). URL: {}, 응답 일부: {}", apiUrl, jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                return Collections.emptyList();
            }
            log.debug("KAMIS API Response (getKindPricesByItemCategoryCode): {}", jsonResponse);


            // TODO: KamisDailyPriceByCategoryListResponse DTO 생성 및 매핑 필요
            // 이 부분은 실제 API 응답을 확인하고 적절한 DTO로 수정해야 합니다.
            // 현재 KamisDailyCountyListResponse DTO를 사용하고 있으므로, 응답 구조가 다르면 파싱 오류가 발생할 수 있습니다.
            // 실제 응답에 맞는 DTO를 만들거나, 또는 이 DTO가 호환되는지 확인이 필요합니다.
            KamisDailyCountyListResponse apiResponse;
            try {
                apiResponse = objectMapper.readValue(jsonResponse, KamisDailyCountyListResponse.class);
            } catch (Exception e) {
                log.error("JSON 파싱 오류 (getKindPricesByItemCategoryCode). 응답: {}. URL: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)), apiUrl, e);
                return Collections.emptyList();
            }

            if (apiResponse == null || !"000".equals(apiResponse.getErrorCode()) || apiResponse.getPrice() == null) {
                 log.warn("KAMIS API 에러 또는 데이터 없음 (getKindPricesByItemCategoryCode). ErrorCode: {}, URL: {}", apiResponse != null ? apiResponse.getErrorCode() : "N/A", apiUrl);
                return Collections.emptyList();
            }

            for (KamisDailyCountyListResponse.PriceDetailItem item : apiResponse.getPrice()) {
                 if (item.getDpr1() == null || item.getDpr1().isEmpty() || item.getDpr1().equals("-") || item.getDpr1().equals("[]") ||
                    item.getDpr2() == null || item.getDpr2().isEmpty() || item.getDpr2().equals("-") || item.getDpr2().equals("[]")) {
                    log.trace("가격 정보 누락 (getKindPricesByItemCategoryCode): {}", item.getProductName());
                    continue;
                }

                int todayPrice = 0;
                int yesterdayPrice = 0;
                try {
                    todayPrice = Integer.parseInt(item.getDpr1().replace(",", ""));
                    yesterdayPrice = Integer.parseInt(item.getDpr2().replace(",", ""));
                } catch (NumberFormatException e) {
                    log.warn("가격 파싱 오류 (getKindPricesByItemCategoryCode): dpr1={}, dpr2={}", item.getDpr1(), item.getDpr2(), e);
                    continue;
                }

                double priceChangeRate = 0.0;
                if (yesterdayPrice != 0 && item.getValue() != null && !item.getValue().isEmpty() && !item.getValue().equals("-") && item.getDirection() != null && !item.getDirection().isEmpty()) {
                    try {
                        priceChangeRate = Double.parseDouble(item.getValue());
                        if ("0".equals(item.getDirection())) { // 하락
                            priceChangeRate = -priceChangeRate;
                        } else if ("2".equals(item.getDirection())) { // 보합
                            priceChangeRate = 0.0;
                        }
                        // "1" (상승)의 경우 양수 그대로 사용
                    } catch (NumberFormatException e) {
                        log.warn("등락율 파싱 오류 (getKindPricesByItemCategoryCode): {}", item.getValue(), e);
                    }
                }


                KindPriceDto dto = KindPriceDto.builder()
                        .itemCode(item.getProductno()) // 실제 API 응답에 따라 itemCode, kindCode 매핑 필요
                        .kindCode(item.getCategoryCode()) // API 응답의 category_code 또는 kind_code 등으로 변경 필요
                        .kindName(item.getItemName()) // API 응답의 item_name, kind_name 등으로 변경 필요
                        .unit(item.getUnit())
                        .todayPrice(todayPrice)
                        .yesterdayPrice(yesterdayPrice)
                        .priceChangeRate(priceChangeRate)
                        .priceDirection(item.getDirection() != null && !item.getDirection().isEmpty() ? Integer.parseInt(item.getDirection()) : null)
                        .latestDate(item.getLastestDay())
                        .build();
                kindPrices.add(dto);
                log.trace("KindPriceDto 추가됨: {}", dto);
            }
            log.info("총 {}개의 KindPriceDto 생성 완료 (getKindPricesByItemCategoryCode).", kindPrices.size());

        } catch (Exception e) {
            log.error("KAMIS API 호출 또는 외부 오류 (getKindPricesByItemCategoryCode). URL: {}", apiUrl, e);
            return Collections.emptyList();
        }
        return kindPrices;
    }

    public List<DailyPrice> getDailyPrices(String itemCategoryCode, String itemCode, String kindCode, int year, int month) {
        log.info("DB에서 일별 가격 조회: {}, {}, {}, 년: {}, 월: {}", itemCategoryCode, itemCode, kindCode, year, month);
        // DailyPriceRepository에 정의된 메서드 시그니처와 일치해야 합니다.
        // 예를 들어, Information 엔티티를 직접 사용하거나, 각 코드와 연/월을 파라미터로 받는 메서드가 필요합니다.
        // 현재는 Information 객체를 통해 조회하는 메서드가 없으므로, 리포지토리에 추가하거나
        // 아래와 같이 각 필드를 직접 사용하는 메서드를 호출해야 합니다. (findByYearAndMonthAndInformationDetails 같은)
        // return dailyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeAndYearAndMonthOrderByDayAsc(itemCategoryCode, itemCode, kindCode, String.valueOf(year), String.format("%02d", month));
        // 임시로 빈 리스트 반환, 실제 구현 필요
        return Collections.emptyList();
    }

    public List<MonthlyPrice> getMonthlyPrices(String itemCategoryCode, String itemCode, String kindCode, int startYear, int startMonth, int endYear, int endMonth) {
        log.info("DB에서 월별 가격 조회: {}, {}, {}, 기간: {}-{} ~ {}-{}", itemCategoryCode, itemCode, kindCode, startYear, startMonth, endYear, endMonth);
        return monthlyPriceRepository.findPricesInDateRange(itemCategoryCode, itemCode, kindCode, startYear, startMonth, endYear, endMonth);
    }

    public List<YearlyPrice> getYearlyPrices(String itemCategoryCode, String itemCode, String kindCode) {
        log.info("DB에서 연도별 가격 조회: {}, {}, {}", itemCategoryCode, itemCode, kindCode);
        return yearlyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAsc(itemCategoryCode, itemCode, kindCode);
    }
}