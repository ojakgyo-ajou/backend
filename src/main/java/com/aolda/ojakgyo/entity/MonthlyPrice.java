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
@Table(name = "monthly_price")
public class MonthlyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_year")
    private Integer priceYear;

    @Column(name = "price_month")
    private Integer priceMonth;
    
    @Column(name = "price")
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id") // Information 엔티티의 ID를 참조
    private Information information; // 필드 타입을 Products에서 Information으로 변경하고, 필드명도 information으로 변경
}