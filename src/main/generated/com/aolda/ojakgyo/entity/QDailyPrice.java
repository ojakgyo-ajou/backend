package com.aolda.ojakgyo.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDailyPrice is a Querydsl query type for DailyPrice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyPrice extends EntityPathBase<DailyPrice> {

    private static final long serialVersionUID = -2025917435L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDailyPrice dailyPrice = new QDailyPrice("dailyPrice");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInformation information;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public QDailyPrice(String variable) {
        this(DailyPrice.class, forVariable(variable), INITS);
    }

    public QDailyPrice(Path<? extends DailyPrice> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDailyPrice(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDailyPrice(PathMetadata metadata, PathInits inits) {
        this(DailyPrice.class, metadata, inits);
    }

    public QDailyPrice(Class<? extends DailyPrice> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.information = inits.isInitialized("information") ? new QInformation(forProperty("information")) : null;
    }

}

