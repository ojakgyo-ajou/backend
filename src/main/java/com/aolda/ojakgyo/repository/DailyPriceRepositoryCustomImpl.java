package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.DailyPrice;
import com.aolda.ojakgyo.entity.QDailyPrice;
import com.aolda.ojakgyo.entity.QInformation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyPriceRepositoryCustomImpl implements DailyPriceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DailyPrice> findByYearAndMonthOrderByDateAsc(String itemCategoryCode, String itemCode, String kindCode, int year, int month) {
        QDailyPrice dailyPrice = QDailyPrice.dailyPrice;
        QInformation information = QInformation.information;

        return queryFactory
                .selectFrom(dailyPrice)
                .join(dailyPrice.information, information)
                .where(
                        information.itemCategoryCode.eq(itemCategoryCode)
                                .and(information.itemCode.eq(itemCode))
                                .and(information.kindCode.eq(kindCode))
                                .and(dailyPrice.year.eq(String.valueOf(year)))
                                .and(dailyPrice.month.eq(String.valueOf(month)))
                )
                .orderBy(
                        dailyPrice.year.asc(),
                        dailyPrice.month.asc(),
                        dailyPrice.day.asc()
                )
                .fetch();
    }
} 