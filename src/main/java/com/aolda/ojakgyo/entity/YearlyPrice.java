package com.aolda.ojakgyo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

@Entity
@Getter
@Builder
@Table(name = "yearly_price")
@NoArgsConstructor
@AllArgsConstructor
public class YearlyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id") // 실제 Products 테이블의 PK 컬럼명에 맞춰야 합니다.
    private Products products;

    // 연도
    @Column(name = "year")
    private Integer year;

    // 연도별 평균 가격
    @Column(name = "price")
    private Integer price;
}