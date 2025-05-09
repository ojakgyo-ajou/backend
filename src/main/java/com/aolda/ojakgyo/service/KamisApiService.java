package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.DailyDto;
import com.aolda.ojakgyo.entity.Daily;
import com.aolda.ojakgyo.repository.DailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KamisApiService {

    @Value("${kamis.api.cert-key}")
    private String p_cert_key;

    @Value("${kamis.api.cert-id}")
    private String p_cert_id;

    private final DailyRepository dailyRepository;

    // 만약 DB가 비어 있다면, 금일자의 농산물 가격 정보 조회하여 DB에 저장.
    public String getKamisApi() {

        String url = "http://www.kamis.or.kr/service/price/xml.do?action=dailySalesList&p_cert_key=" + p_cert_key + "&p_cert_id=" + p_cert_id + "&p_returntype=json";

        RestTemplate restTemplate = new RestTemplate();

        String response = null;

        try {
            response = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            e.printStackTrace(); // 간단한 예외 출력
            return null; // 또는 예외 상황에 맞는 다른 값을 반환
        }

        // json 파싱 (요청에 따라 일단 처리하지 않음)
        return response;
    }

    // 일


    // 월

    // 년도별

    // [할인추천 대시보드용] 금일 모든 농산물 가격을 조회 (전일 대비 가장 많이 할인된 품목 20개까지 조회 )
    public List<DailyDto> getDailyTop20DiscountedProducts() {
        Pageable topList = PageRequest.of(0, 20);
        List<Daily> results = dailyRepository.findTopByOrderByDayPriceDifferenceDesc(topList);

        if (results.isEmpty()) {
            return Collections.emptyList(); // 결과가 없으면 빈 리스트 반환
        }

        return results.stream()
                .map(daily -> new DailyDto(
                        daily.getProductName(),
                        daily.getItemName(),
                        daily.getUnit(),
                        daily.getLatestDate(),
                        daily.getDpr1(),
                        daily.getDpr2()
                ))
                .collect(Collectors.toList());
    }

    // 수정 필요
    public Optional<Daily> getMonthlyTopDiscountedProduct() {
        Pageable topOne = PageRequest.of(0, 1);
        List<Daily> results = dailyRepository.findTopByOrderByMonthPriceDifferenceDesc(topOne);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Daily> getYearlyTopDiscountedProduct() {
        Pageable topOne = PageRequest.of(0, 1);
        List<Daily> results = dailyRepository.findTopByOrderByYearPriceDifferenceDesc(topOne);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
