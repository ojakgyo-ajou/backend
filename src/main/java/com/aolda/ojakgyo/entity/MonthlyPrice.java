package com.aolda.ojakgyo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monthly_price") // 테이블 이름 예시 (기존 정보 기반)
public class MonthlyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_year") // 연도를 저장할 컬럼
    private Integer priceYear;

    @Column(name = "price_month") // 월을 저장할 컬럼
    private Integer priceMonth; // 필드명을 priceMonth로 변경 (카멜케이스 일관성)

    @Column(name = "price")
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id") // 실제 Products 테이블의 PK 컬럼명에 맞춰야 합니다.
    private Products products;
}