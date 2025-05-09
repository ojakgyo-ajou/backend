package com.aolda.ojakgyo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "information")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "information_id")
    private Long id;

    // unit 단위 (kg)
    private String unit;

    // size 단위 수치 (600)
    private int size;

    // p_itemcategorycode 부류코드
    @Column(name = "itemcategorycode")
    private String itemCategoryCode;

    // 부류 이름 
    @Column(name = "itemcategoryname")
    private String itemCategoryName;

    // p_itemcode 품목코드
    @Column(name = "itemcode")
    private String itemCode;

    // p_kindcode 품종코드
    @Column(name = "kindcode")
    private String kindCode;

    @Column(name = "itemname") // 품목명
    private String itemName;

    @Column(name = "kindname") // 품종명
    private String kindName;
}
