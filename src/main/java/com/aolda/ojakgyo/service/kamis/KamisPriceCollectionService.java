package com.aolda.ojakgyo.service.kamis;

import com.aolda.ojakgyo.dto.kamis.KamisMonthlyApiBaseResponse;
import com.aolda.ojakgyo.dto.kamis.KamisPriceDataDto;
import com.aolda.ojakgyo.dto.kamis.KamisPriceItemMonthlyDto;
import com.aolda.ojakgyo.entity.Information;
import com.aolda.ojakgyo.entity.MonthlyPrice;
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
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class KamisPriceCollectionService {

    private final InformationRepository informationRepository;
    private final MonthlyPriceRepository monthlyPriceRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient; // RestClient 주입 또는 생성자에서 빌드

    @Value("${kamis.api.cert-key}")
    private String certKey;

    @Value("${kamis.api.cert-id}")
    private String certId;

    private static final String KAMIS_API_URL = "https://www.kamis.or.kr/service/price/xml.do";

    // 생성자에서 RestClient 초기화 (필요시 @Autowired RestClient.Builder builder 사용 가능)
    public KamisPriceCollectionService(InformationRepository informationRepository,
                                       MonthlyPriceRepository monthlyPriceRepository,
                                       ObjectMapper objectMapper) {
        this.informationRepository = informationRepository;
        this.monthlyPriceRepository = monthlyPriceRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
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
                .queryParam("p_yyyy", "2024") // 기준년도
                .queryParam("p_period", "3")  // 최근 6년치 (2020~2025).
                .queryParam("p_itemcategorycode", information.getItemCategoryCode())
                .queryParam("p_itemcode", information.getItemCode())
                .queryParam("p_kindcode", String.format("%02d", Integer.parseInt(information.getKindCode())))
                .queryParam("p_convert_kg_yn", "Y")
                .queryParam("p_cert_key", certKey)
                .queryParam("p_cert_id", certId)
                .queryParam("p_returntype", "json");

        String apiUrl = uriBuilder.toUriString();
        log.debug("Requesting API URL: {}", apiUrl);
        
        // API 호출
        try {
            String jsonResponse = restClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .body(String.class);

            if (jsonResponse == null) {
                log.warn("API 응답이 null입니다. 품목: {}/{}/{}", information.getItemCategoryCode(), information.getItemCode(), information.getKindCode());
                return;
            }

            log.debug("API Response for {}/{}/{}: {}", information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), jsonResponse);

            System.out.println(apiUrl);

            // HTML 응답인지 확인 (간단한 체크)
            if (jsonResponse.trim().startsWith("<")) {
                log.warn("API 응답이 JSON이 아닌 HTML입니다. 품목: {}/{}/{}, 응답 일부: {}",
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                        jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)));
                return;
            }
            
            KamisMonthlyApiBaseResponse apiResponse = objectMapper.readValue(jsonResponse, KamisMonthlyApiBaseResponse.class);

            if (apiResponse == null || !"000".equals(apiResponse.getErrorCode()) || apiResponse.getPrice() == null || apiResponse.getPrice().isEmpty()) {
                String errorCode = (apiResponse != null && apiResponse.getErrorCode() != null) ? apiResponse.getErrorCode() : "N/A";
                String responseDetails = (apiResponse != null && apiResponse.getPrice() == null) ? "Price data is null." :
                                         (apiResponse != null && apiResponse.getPrice().isEmpty()) ? "Price list is empty." :
                                         jsonResponse.substring(0, Math.min(jsonResponse.length(), 200));

                log.warn("API 에러 또는 데이터 없음. 품목: {}/{}/{}, 에러코드: {}, 상세: {}",
                        information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(),
                        errorCode, responseDetails);
                return;
            }


            for (KamisPriceDataDto priceData : apiResponse.getPrice()) {
                if (priceData.getItem() == null || priceData.getItem().isEmpty()) {
                    log.warn("가격 데이터의 항목이 null이거나 비어 있습니다. 품목: {}/{}/{}",
                            information.getItemCategoryCode(), information.getItemCode(), information.getKindCode());
                    continue;
                }
                
                for (KamisPriceItemMonthlyDto priceItem : priceData.getItem()) {
                    Integer year = Integer.parseInt(priceItem.getYyyy());

                    LocalDateTime now = LocalDateTime.now();
                    int currentYear = now.getYear();
                    int currentMonth = now.getMonthValue();

                    if (year > currentYear) {
                         log.warn("미래 연도의 데이터입니다. 건너뜁니다. 품목: {}/{}/{}, 년도: {}",
                                information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), year);
                        continue;
                    }

                    String[] monthlyPrices = {
                        priceItem.getM1(), priceItem.getM2(), priceItem.getM3(), priceItem.getM4(), priceItem.getM5(), priceItem.getM6(),
                        priceItem.getM7(), priceItem.getM8(), priceItem.getM9(), priceItem.getM10(), priceItem.getM11(), priceItem.getM12()
                    };

                    for (int month = 1; month <= 12; month++) {
                        if (year == currentYear && month > currentMonth) {
                            break; // 현재 연도의 현재 월 이후 데이터는 저장하지 않음
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
            }
        } catch (Exception e) {
            log.error("월별 가격 데이터 수집 중 오류 발생. 품목: {}/{}/{}, 오류: {}",
                    information.getItemCategoryCode(), information.getItemCode(), information.getKindCode(), e.getMessage(), e);
        }
    }
}