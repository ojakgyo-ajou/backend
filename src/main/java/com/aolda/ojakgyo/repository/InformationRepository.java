package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // List 임포트 추가
import java.util.Optional; // Optional 임포트 추가

public interface InformationRepository extends JpaRepository<Information, Long> {

    // itemCategoryCode를 기준으로 Information 리스트 조회
    List<Information> findByItemCategoryCode(String itemCategoryCode);

    Optional<Information> findByItemCategoryCodeAndItemCodeAndKindCode(String itemCategoryCode, String itemCode, String kindCode);
}