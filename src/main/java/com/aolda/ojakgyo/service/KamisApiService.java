package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.dto.DailyDto;
import com.aolda.ojakgyo.dto.KindPriceDto; // KindPriceDto 임포트
import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.Information; // Information 임포트
import com.aolda.ojakgyo.entity.MonthlyPrice;
import com.aolda.ojakgyo.entity.YearlyPrice;
import com.aolda.ojakgyo.repository.DailyPriceRepository;
import com.aolda.ojakgyo.repository.InformationRepository; // InformationRepository 임포트
import com.aolda.ojakgyo.repository.MonthlyPriceRepository;
import com.aolda.ojakgyo.repository.YearlyPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // ArrayList 임포트
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

    private final DailyPriceRepository dailyPriceRepository;
    private final MonthlyPriceRepository monthlyPriceRepository;
    private final InformationRepository informationRepository;
    private final YearlyPriceRepository yearlyPriceRepository;

    public List<KindPriceDto> getKindPricesByItemCategoryCode(String itemCategoryCode) {
        return null;
    }

    /**
     * 특정 상품(itemCategoryCode, itemCode, kindCode 기준)의 월별 가격 데이터를 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @return 월별 가격 리스트 (연도, 월 오름차순 정렬)
     */
    public List<MonthlyPrice> getMonthlyPrices(String itemCategoryCode, String itemCode, String kindCode) {
        return monthlyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAscPriceMonthAsc(
                itemCategoryCode, itemCode, kindCode);
    }

    /**
     * 특정 상품(itemCategoryCode, itemCode, kindCode 기준)의 연평균 가격 데이터를 조회합니다.
     *
     * @param itemCategoryCode 부류 코드
     * @param itemCode 품목 코드
     * @param kindCode 품종 코드
     * @return 연평균 가격 리스트 (연도 오름차순 정렬)
     */
    public List<YearlyPrice> getYearlyPrices(String itemCategoryCode, String itemCode, String kindCode) {
        return yearlyPriceRepository.findByInformationItemCategoryCodeAndInformationItemCodeAndInformationKindCodeOrderByPriceYearAsc(
                itemCategoryCode, itemCode, kindCode);
    }
}