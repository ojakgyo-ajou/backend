package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.dto.PageResp;
import com.aolda.ojakgyo.dto.ProductWithReviewScoreDto;
import com.aolda.ojakgyo.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryDSL {

    private final JPAQueryFactory queryFactory;

    public PageResp<ProductWithReviewScoreDto> findProductsByNameOrderByPriceAndReviewScore(String productName) {
        QProducts products = QProducts.products;
        QReview review = QReview.review;
        QUser user = QUser.user;

        List<ProductWithReviewScoreDto> productList = queryFactory
                .select(Projections.constructor(ProductWithReviewScoreDto.class,
                        products.productId,
                        user,
                        products.productName,
                        products.productPrice,
                        Expressions.numberTemplate(Double.class, "AVG({0})", review.score).as("averageScore")))
                .from(products)
                .leftJoin(products.user, user)
                .leftJoin(review).on(review.products.eq(products))
                .where(products.productName.containsIgnoreCase(productName))
                .groupBy(products.productId, user, products.productName, products.productPrice)
                .orderBy(
                        products.productPrice.asc(),
                        Expressions.numberTemplate(Double.class, "AVG({0})", review.score).desc()
                )
                .fetch();

        return PageResp.<ProductWithReviewScoreDto>builder()
                .content(productList)
                .build();
    }
}
