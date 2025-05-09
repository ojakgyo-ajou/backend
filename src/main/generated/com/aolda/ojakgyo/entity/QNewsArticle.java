package com.aolda.ojakgyo.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNewsArticle is a Querydsl query type for NewsArticle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNewsArticle extends EntityPathBase<NewsArticle> {

    private static final long serialVersionUID = -1654745106L;

    public static final QNewsArticle newsArticle = new QNewsArticle("newsArticle");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath cropName = createString("cropName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath link = createString("link");

    public final StringPath press = createString("press");

    public final StringPath pubDate = createString("pubDate");

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QNewsArticle(String variable) {
        super(NewsArticle.class, forVariable(variable));
    }

    public QNewsArticle(Path<? extends NewsArticle> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNewsArticle(PathMetadata metadata) {
        super(NewsArticle.class, metadata);
    }

}

