package com.aolda.ojakgyo.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMonthlyPrice is a Querydsl query type for MonthlyPrice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMonthlyPrice extends EntityPathBase<MonthlyPrice> {

    private static final long serialVersionUID = 1920489809L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMonthlyPrice monthlyPrice = new QMonthlyPrice("monthlyPrice");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInformation information;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> priceMonth = createNumber("priceMonth", Integer.class);

    public final NumberPath<Integer> priceYear = createNumber("priceYear", Integer.class);

    public QMonthlyPrice(String variable) {
        this(MonthlyPrice.class, forVariable(variable), INITS);
    }

    public QMonthlyPrice(Path<? extends MonthlyPrice> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMonthlyPrice(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMonthlyPrice(PathMetadata metadata, PathInits inits) {
        this(MonthlyPrice.class, metadata, inits);
    }

    public QMonthlyPrice(Class<? extends MonthlyPrice> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.information = inits.isInitialized("information") ? new QInformation(forProperty("information")) : null;
    }

}

