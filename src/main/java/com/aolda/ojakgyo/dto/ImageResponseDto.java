package com.aolda.ojakgyo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageResponseDto {
    private String imageName;
    private String imageUrl;
    private String originalFileName;
    private Long fileSize;
} 