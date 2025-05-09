package com.aolda.ojakgyo.service.kamis;

import com.aolda.ojakgyo.dto.kamis.KamisApiResponse;
import com.aolda.ojakgyo.dto.kamis.KamisMonthlyApiBaseResponse;
import com.aolda.ojakgyo.dto.kamis.KamisPriceDataDto;
import com.aolda.ojakgyo.dto.kamis.KamisPriceItemMonthlyDto;
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice;
import com.aolda.ojakgyo.repository.DailyPriceRepository;
import com.aolda.ojakgyo.repository.InformationRepository;
import com.aolda.ojakgyo.repository.MonthlyPriceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class KamisPriceCollectionService {

    private final InformationRepository informationRepository;
    private final MonthlyPriceRepository monthlyPriceRepository;
    private final DailyPriceRepository dailyPriceRepository; // DailyPriceRepository 주입
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${kamis.api.cert-key}")
    private String certKey;

    @Value("${kamis.api.cert-id}")
    private String certId;

    private static final String KAMIS_API_URL = "https://www.kamis.or.kr/service/price/xml.do";

    public KamisPriceCollectionService(InformationRepository informationRepository,
                                       MonthlyPriceRepository monthlyPriceRepository,
                                       DailyPriceRepository dailyPriceRepository, // 생성자에 추가
                                       ObjectMapper objectMapper) {
        this.informationRepository = informationRepository;
        this.monthlyPriceRepository = monthlyPriceRepository;
        this.dailyPriceRepository = dailyPriceRepository; // 필드 초기화 
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }
    /**
     * API로부터 받은 JSON 응답 문자열을 파싱하여 DailyPrice 데이터를 저장합니다.
     *
     * @param jsonResponse API 응답 JSON 문자열
     */
    @Transactional
    public void saveDailyPricesFromJsonResponse(String jsonResponse) {
        try {
            KamisApiResponse apiResponse = objectMapper.readValue(jsonResponse, KamisApiResponse.class);

            if (apiResponse == null || apiResponse.getData() == null || apiResponse.getCondition() == null || apiResponse.getCondition().isEmpty()) {
                log.warn("API 응답의 필수 필드가 null입니다. 응답: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));
                return;
            }

            KamisApiResponse.ItemData itemData = null;
            String errorCode = null;

            Object rawData = apiResponse.getData();
            if (rawData instanceof List) {
                // data 필드가 리스트 형태로 오는 경우 (예: ["200"] 또는 ["ERR-001"])
                List<?> dataList = (List<?>) rawData;
                if (!dataList.isEmpty() && dataList.get(0) instanceof String) {
                    errorCode = (String) dataList.get(0);
                    // itemData는 null로 유지하거나, errorCode만 가진 ItemData 객체 생성 가능
                    // 여기서는 errorCode만 추출하고, itemData는 null로 둔 후 아래에서 처리
                    log.info("KAMIS API가 data 필드를 배열로 반환했습니다. 첫 번째 요소(에러/상태 코드 추정): {}", errorCode);
                    if ("200".equals(errorCode) || "ERR-001".equals(errorCode)) { // "200"은 보통 성공이나 여기서는 다른 의미일 수 있음, API 문서 확인 필요
                        // "데이터 없음" 등의 특정 코드에 대한 처리
                        KamisApiResponse.ConditionItem firstCondition = apiResponse.getCondition().isEmpty() ? null : apiResponse.getCondition().get(0);
                        log.info("KAMIS API 응답: 데이터가 없거나 특정 상태 코드 반환. 코드: {}, 조건: {}", errorCode, firstCondition);
                        return;
                    }
                }
            } else if (rawData instanceof java.util.Map) {
                // data 필드가 객체 형태로 오는 경우, ItemData로 변환 시도
                try {
                    itemData = objectMapper.convertValue(rawData, KamisApiResponse.ItemData.class);
                    errorCode = itemData.getErrorCode();
                } catch (IllegalArgumentException e) {
                    log.warn("apiResponse.getData()를 ItemData로 변환 실패. rawData 타입: {}, 오류: {}", rawData.getClass().getName(), e.getMessage());
                    // 변환 실패 시 errorCode는 null일 수 있으므로, 이후 로직에서 null 체크 필요
                }
            }

            // errorCode를 기준으로 처리 (itemData가 null일 수도 있음)
            if (errorCode != null && !"000".equals(errorCode) && !"".equals(errorCode)) {
                log.warn("API 에러 발생. 에러 코드: {}, 응답: {}", errorCode, jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));
                return;
            }
            
            // itemData가 정상적으로 파싱되었고, errorCode가 "000"인 경우 또는 errorCode가 null이지만 itemData가 있는 경우 (오류 아닌 데이터 없음 등)
            if (itemData != null && "000".equals(itemData.getErrorCode())) {
                 if (itemData.getItem() == null || itemData.getItem().isEmpty()) {
                    if (jsonResponse.contains("데이터가 존재하지 않습니다.")) {
                        KamisApiResponse.ConditionItem firstCondition = apiResponse.getCondition().isEmpty() ? null : apiResponse.getCondition().get(0);
                        log.info("KAMIS API 응답: 데이터가 존재하지 않습니다. (itemData.item이 비었음) 조건: {}", firstCondition);
                        return;
                    }
                    // 데이터가 없는 다른 케이스 로그 (예: 빈 item 리스트)
                    log.info("KAMIS API 응답: itemData.item이 null이거나 비어있습니다. errorCode: 000. 응답 일부: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                    return; // 데이터가 없으므로 더 이상 처리하지 않음
                 }
            } else if (itemData == null && "000".equals(errorCode)){
                 log.info("KAMIS API 응답: errorCode는 000이나 ItemData 파싱에 실패했거나 item 리스트가 없습니다. 응답 일부: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                 return;
            } else if (itemData == null && errorCode != null && !"000".equals(errorCode)) {
                // 위에서 이미 처리되었지만, 방어적으로 추가
                log.warn("API 에러 발생 (itemData null). 에러 코드: {}, 응답: {}", errorCode, jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));
                return;
            } else if (itemData == null && errorCode == null) {
                log.warn("KAMIS API 응답에서 data 필드를 해석할 수 없습니다. 응답 일부: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));
                return;
            }

            // itemData가 null이거나 itemData.getItem()이 null이면 여기서 NPE 발생 가능하므로 방어 코드 추가
            if (itemData == null || itemData.getItem() == null) {
                log.warn("최종적으로 유효한 가격 데이터(itemData 또는 itemData.item)를 얻지 못했습니다. errorCode: {}. 응답: {}", errorCode, jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));
                return;
            }


            KamisApiResponse.ConditionItem conditionItem = apiResponse.getCondition().get(0);
            Optional<Information> optInformation = informationRepository.findByItemCategoryCodeAndItemCodeAndKindCode(
                    conditionItem.getPItemcategorycode(),
                    conditionItem.getPItemcode(),
                    conditionItem.getPKindcode()
            );

            if (optInformation.isEmpty()) {
                log.warn("해당 조건에 맞는 Information 정보를 찾을 수 없습니다: 카테고리코드={}, 품목코드={}, 품종코드={}",
                        conditionItem.getPItemcategorycode(), conditionItem.getPItemcode(), conditionItem.getPKindcode());
                return;
            }
            Information information = optInformation.get();

            List<KamisApiResponse.PriceItem> priceItems = itemData.getItem();
            if (priceItems == null || priceItems.isEmpty()) {
                log.info("가격 데이터 리스트가 비어있거나 null입니다. Information ID: {}", information.getId());
                return;
            }

            for (KamisApiResponse.PriceItem priceItem : priceItems) {
                // "평균", "평년" 데이터와 같이 지역 특정적이지 않은 데이터는 건너뜁니다.
                // 또한, itemname 또는 kindname이 없는 데이터도 건너뜁니다.
                if (priceItem.getItemname() == null || priceItem.getItemname().isEmpty() ||
                    priceItem.getKindname() == null || priceItem.getKindname().isEmpty() ||
                    "평균".equals(priceItem.getCountyname()) || "평년".equals(priceItem.getCountyname())) {
                    log.trace("평균/평년 데이터, 또는 itemname/kindname이 없는 데이터는 건너뜁니다: {}", priceItem);
                    continue;
                }

                String year = priceItem.getYyyy();
                if (priceItem.getRegday() == null || !priceItem.getRegday().contains("/")) {
                    log.warn("잘못된 regday 형식입니다: {}. 건너뜁니다. (Item: {})", priceItem.getRegday(), priceItem);
                    continue;
                }
                String[] regDayParts = priceItem.getRegday().split("/"); // "MM/dd"
                if (regDayParts.length != 2) {
                    log.warn("regday 파싱 실패: {}. 건너뜁니다. (Item: {})", priceItem.getRegday(), priceItem);
                    continue;
                }
                String month = regDayParts[0];
                String day = regDayParts[1];

                // 5월 데이터만 저장하도록 필터링
                if (!"05".equals(month)) {
                    log.trace("5월 데이터가 아니므로 건너뜁니다. 날짜: {}/{}/{}, Item: {}", year, month, day, priceItem);
                    continue;
                }

                Integer priceValue;
                try {
                    if (priceItem.getPrice() == null || "-".equals(priceItem.getPrice().trim()) || priceItem.getPrice().trim().isEmpty()) {
                        log.trace("가격 데이터가 없습니다('-' 또는 빈 문자열). Information ID {}, Date {}/{}/{}, Item: {}",
                                information.getId(), year, month, day, priceItem);
                        continue;
                    }
                    priceValue = Integer.parseInt(priceItem.getPrice().replace(",", ""));
                } catch (NumberFormatException e) {
                    log.warn("가격 문자열 파싱 실패: '{}' for Information ID {}. 건너뜁니다. (Item: {})",
                            priceItem.getPrice(), information.getId(), priceItem, e);
                    continue;
                }

                DailyPrice dailyPrice = DailyPrice.builder()
                        .information(information)
                        .year(year)
                        .month(month)
                        .day(day)
                        .price(priceValue)
                        .build();

                dailyPriceRepository.save(dailyPrice);
            }
            log.info("Information ID {}에 대한 DailyPrice 데이터 처리가 완료되었습니다.", information.getId());

        } catch (Exception e) {
            log.error("DailyPrice JSON 데이터 처리 중 오류 발생: {}", e.getMessage(), e);
            log.error("오류 발생 JSON: {}", jsonResponse.substring(0, Math.min(jsonResponse.length(), 1000)));
        }
    }

    @Transactional
    public void collectMonthlyPriceDataForAllInformation() {
        List<Information> allInformation = informationRepository.findAll();
        log.info("총 {}개의 품목에 대해 월별 가격 데이터 수집을 시작합니다.", allInformation.size());

        for (Information info : allInformation) {
            collectMonthlyPriceDataForInformation(info);
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        log.info("모든 품목에 대한 월별 가격 데이터 수집이 완료되었습니다.");
    }

    private void collectMonthlyPriceDataForInformation(Information information) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAMIS_API_URL)
                .queryParam("action", "monthlySalesList")
                .queryParam("p_yyyy", String.valueOf(LocalDateTime.now().getYear())) // 기준년도
                .queryParam("p_period", "3")  // 최근 3년치 데이터 요청
                .queryParam("p_itemcategorycode", information.getItemCategoryCode())
                .queryParam("p_itemcode", information.getItemCode())
                .queryParam("p_kindcode", String.format("%02d", Integer.parseInt(information.getKindCode())))
                .queryParam("p_convert_kg_yn", "Y")
                .queryParam("p_cert_key", certKey)
                .queryParam("p_cert_id", certId)
                .queryParam("p_returntype", "json");

        String apiUrl = uriBuilder.toUriString();
        log.debug("Requesting API URL for monthly data: {}", apiUrl);
        
        try {
            String jsonResponse = restClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .body(String.class);

            if (jsonResponse == null) {
                log.warn("월별 데이터 API 응답이 null입니다. 품목: {}/{}/{}", information.getItemCategoryCode(), information.getItemCode(), information.getKindCode());
                return;
            }

            log.debug("월별 데이터 API Response for {}/{}/{}: {}", information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));


            if (jsonResponse.trim().startsWith("<")) {
                log.warn("월별 데이터 API 응답이 JSON이 아닌 HTML입니다. 품목: {}/{}/{}, 응답 일부: {}",
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                        jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                return;
            }
            
            KamisMonthlyApiBaseResponse apiResponse = objectMapper.readValue(jsonResponse, KamisMonthlyApiBaseResponse.class);

            if (apiResponse == null || (!"000".equals(apiResponse.getErrorCode()) && !"".equals(apiResponse.getErrorCode())) || apiResponse.getPrice() == null || apiResponse.getPrice().isEmpty()) {
                String errorCode = (apiResponse != null && apiResponse.getErrorCode() != null) ? apiResponse.getErrorCode() : "N/A";
                String responseDetails;
                if (apiResponse != null) {
                    if (jsonResponse.contains("데이터가 존재하지 않습니다.")) {
                         log.info("KAMIS 월별 API 응답: 데이터가 존재하지 않습니다. 품목: {}/{}/{}", information.getItemCategoryCode(), information.getItemCode(), information.getKindCode());
                         return;
                    }
                    responseDetails = (apiResponse.getPrice() == null) ? "Price data is null." :
                                         (apiResponse.getPrice().isEmpty()) ? "Price list is empty." :
                                          jsonResponse.substring(0, Math.min(jsonResponse.length(), 200));
                } else {
                    responseDetails = jsonResponse.substring(0, Math.min(jsonResponse.length(), 200));
                }


                log.warn("월별 데이터 API 에러 또는 데이터 없음. 품목: {}/{}/{}, 에러코드: {}, 상세: {}",
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                        errorCode, responseDetails);
                return;
            }


            for (KamisPriceDataDto priceData : apiResponse.getPrice()) {
                if (priceData.getItem() == null || priceData.getItem().isEmpty()) {
                    log.warn("월별 가격 데이터의 항목이 null이거나 비어 있습니다. 품목: {}/{}/{}",
                            information.getItemCategoryCode(), information.getItemCode(), information.getKindCode());
                    continue;
                }
                
                for (KamisPriceItemMonthlyDto priceItem : priceData.getItem()) {
                    Integer year = Integer.parseInt(priceItem.getYyyy());

                    LocalDateTime now = LocalDateTime.now();
                    int currentYear = now.getYear();
                    int currentMonth = now.getMonthValue();

                    if (year > currentYear) {
                         log.warn("미래 연도의 월별 데이터입니다. 건너뜁니다. 품목: {}/{}/{}, 년도: {}",
                                information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), year);
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
                            continue; 
                        }

                        try {
                            Integer price = Integer.parseInt(priceStr.replace(",", ""));

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
                            log.warn("월별 가격 문자열 파싱 실패: '{}' for {}/{}/{} Year: {}, Month: {}",
                                    priceStr, information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), year, month, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("월별 가격 데이터 수집 중 오류 발생. 품목: {}/{}/{}, 오류: {}",
                    information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), e.getMessage(), e);
        }
    }

    @Transactional
    public void collectDailyPriceDataForInformation(Information information, String targetYear, String targetMonth, String targetDay) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAMIS_API_URL)
                .queryParam("action", "dailyPriceByCategoryList")
                .queryParam("p_yyyy", targetYear)
                .queryParam("p_period", "1") // 일별 데이터는 보통 하루치, 필요시 조절 가능
                .queryParam("p_itemcategorycode", information.getItemCategoryCode())
                .queryParam("p_itemcode", information.getItemCode())
                .queryParam("p_kindcode", String.format("%02d", Integer.parseInt(information.getKindCode())))
                .queryParam("p_productclscode", "01") // 소매 가격
                .queryParam("p_productrankcode", "04") // 상품 등급
                .queryParam("p_regday", String.format("%s/%s", targetMonth, targetDay)) // M/d 형식 요청
                .queryParam("p_convert_kg_yn", "Y") // kg 단위 변환
                .queryParam("p_cert_key", certKey)
                .queryParam("p_cert_id", certId)
                .queryParam("p_returntype", "json");

        String apiUrl = uriBuilder.toUriString();
        log.info("Requesting API URL for daily data: {}", apiUrl);

        try {
            String jsonResponse = restClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .body(String.class);

            if (jsonResponse == null) {
                log.warn("일별 데이터 API 응답이 null입니다. 품목: {}/{}/{}, 날짜: {}/{}/{}",
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                        targetYear, targetMonth, targetDay);
                return;
            }

            log.debug("일별 데이터 API Response for {}/{}/{}: {}",
                    information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                    jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)));

            if (jsonResponse.trim().startsWith("<")) {
                log.warn("일별 데이터 API 응답이 JSON이 아닌 다른 형식(예: HTML)입니다. 품목: {}/{}/{}, 응답 일부: {}",
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                        jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                return;
            }

            // "p_productclscode", "01" (소매) 또는 "02" (도매) 와 같은 파라미터를 추가로 고려해야 할 수 있습니다.
            // 여기서는 기본값을 사용하거나, 필요에 따라 파라미터로 받도록 수정할 수 있습니다.
            // 아래는 예시로 "소매" 기준 데이터를 가져오는 경우입니다.
            // 실제 API 명세에 따라 p_productrankcode (04:상품, 05:중품), p_countycode (도매시장코드) 등도 필요할 수 있습니다.

            // API 응답에 따라 적절한 DTO를 사용해야 합니다. KamisApiResponse가 적합한지 확인 필요.
            // 만약 응답 구조가 다르다면, 새로운 DTO를 생성해야 합니다.
            saveDailyPricesFromJsonResponse(jsonResponse); // 기존 파싱 및 저장 로직 재활용

        } catch (Exception e) {
            log.error("일별 가격 데이터 수집 중 오류 발생. 품목: {}/{}/{}, 날짜: {}/{}/{}, 오류: {}",
                    information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                    targetYear, targetMonth, targetDay, e.getMessage(), e);
        }
    }

    @Transactional
    public void collectDailyPriceDataForAllInformationToday() {
        List<Information> allInformation = informationRepository.findAll();
        LocalDate today = LocalDate.now();
        String targetYear = String.valueOf(today.getYear());
        String targetMonth = String.valueOf(today.getMonthValue());
        String targetDay = String.valueOf(today.getDayOfMonth());

        for (Information information : allInformation) {
            // 이미 오늘 날짜 데이터가 있는지 확인 (선택 사항)
            // if (dailyPriceRepository.existsByInformationAndDate(information, today)) {
            // log.info("이미 오늘자 일별 데이터가 존재합니다: {}", information.getItemName());
            // continue;
            // }
            collectDailyPriceDataForInformation(information, targetYear, targetMonth, targetDay);
        }
        log.info("오늘 날짜의 모든 품목에 대한 일별 가격 데이터 수집 완료.");
    }
}