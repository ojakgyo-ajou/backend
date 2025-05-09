package com.aolda.ojakgyo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@Table(name = "daily_price")
@NoArgsConstructor
@AllArgsConstructor
public class DailyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "information_id")
    private Information information;

    // 날짜 
    @Column(name = "year")
    private String year;

    @Column(name = "month")
    private String month;

    @Column(name = "day")
    private String day;

    // 가격
    @Column(name = "price")
    private Integer price;
}