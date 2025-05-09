package com.aolda.ojakgyo.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInformation is a Querydsl query type for Information
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInformation extends EntityPathBase<Information> {

    private static final long serialVersionUID = -370248073L;

    public static final QInformation information = new QInformation("information");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath itemCategoryCode = createString("itemCategoryCode");

    public final StringPath itemCategoryName = createString("itemCategoryName");

    public final StringPath itemCode = createString("itemCode");

    public final StringPath itemName = createString("itemName");

    public final StringPath kindCode = createString("kindCode");

    public final StringPath kindName = createString("kindName");

    public final NumberPath<Integer> size = createNumber("size", Integer.class);

    public final StringPath unit = createString("unit");

    public QInformation(String variable) {
        super(Information.class, forVariable(variable));
    }

    public QInformation(Path<? extends Information> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInformation(PathMetadata metadata) {
        super(Information.class, metadata);
    }

}

