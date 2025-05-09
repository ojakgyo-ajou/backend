package com.aolda.ojakgyo.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYearlyPrice is a Querydsl query type for YearlyPrice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QYearlyPrice extends EntityPathBase<YearlyPrice> {

    private static final long serialVersionUID = -904507862L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYearlyPrice yearlyPrice = new QYearlyPrice("yearlyPrice");

    public final NumberPath<Integer> averagePrice = createNumber("averagePrice", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInformation information;

    public final NumberPath<Integer> priceYear = createNumber("priceYear", Integer.class);

    public QYearlyPrice(String variable) {
        this(YearlyPrice.class, forVariable(variable), INITS);
    }

    public QYearlyPrice(Path<? extends YearlyPrice> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYearlyPrice(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYearlyPrice(PathMetadata metadata, PathInits inits) {
        this(YearlyPrice.class, metadata, inits);
    }

    public QYearlyPrice(Class<? extends YearlyPrice> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.information = inits.isInitialized("information") ? new QInformation(forProperty("information")) : null;
    }

}

