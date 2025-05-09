package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.dto.PageResp;
import com.aolda.ojakgyo.dto.ProductDetailDto;
import com.aolda.ojakgyo.dto.ProductWithReviewScoreDto;
import com.aolda.ojakgyo.dto.ReviewDto;
import com.aolda.ojakgyo.entity.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Random;

@Repository
@RequiredArgsConstructor
public class ProductQueryDSL {

    private final JPAQueryFactory queryFactory;
    private final Random random = new Random();

    public PageResp<ProductWithReviewScoreDto> findProductsByNameOrderByPriceAndReviewScore(String productName) {
        QProducts products = QProducts.products;
        QReview review = QReview.review;
        QUser user = QUser.user;

        List<ProductWithReviewScoreDto> productList = queryFactory
                .select(Projections.constructor(ProductWithReviewScoreDto.class,
                        products.productId,
                        user.userId,
                        user.name,
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

    public ProductDetailDto findProductDetailById(Long productId) {
        QProducts products = QProducts.products;
        QReview review = QReview.review;
        QUser user = QUser.user;
        QProducts sellerProducts = new QProducts("sellerProducts");
        QReview sellerReviews = new QReview("sellerReviews");

        // 판매자의 모든 상품에 대한 평균 별점 계산
        Double sellerAverageScore = queryFactory
                .select(Expressions.numberTemplate(Double.class, "AVG({0})", sellerReviews.score))
                .from(sellerProducts)
                .leftJoin(sellerReviews).on(sellerReviews.products.eq(sellerProducts))
                .where(sellerProducts.user.eq(products.user))
                .fetchOne();

        // 해당 상품의 평균 별점 계산
        Double productAverageScore = queryFactory
                .select(Expressions.numberTemplate(Double.class, "AVG({0})", review.score))
                .from(review)
                .where(review.products.productId.eq(productId))
                .fetchOne();

        // 상품 상세 정보와 리뷰 조회
        List<ReviewDto> reviews = queryFactory
                .select(Projections.constructor(ReviewDto.class,
                        review.reviewId,
                        review.score,
                        review.comment,
                        Expressions.constant(random.nextInt(11)))) // 0~10 사이의 랜덤값
                .from(review)
                .where(review.products.productId.eq(productId))
                .orderBy(review.score.desc())
                .fetch();

        // 상품 기본 정보 조회
        ProductDetailDto productDetail = queryFactory
                .select(Projections.constructor(ProductDetailDto.class,
                        products.productId,
                        products.productName,
                        products.productPrice,
                        user.userId,
                        user.name,
                        Expressions.constant(sellerAverageScore),
                        Expressions.constant(productAverageScore),
                        Expressions.constant(reviews)))
                .from(products)
                .leftJoin(products.user, user)
                .where(products.productId.eq(productId))
                .fetchOne();

        return productDetail;
    }
}
