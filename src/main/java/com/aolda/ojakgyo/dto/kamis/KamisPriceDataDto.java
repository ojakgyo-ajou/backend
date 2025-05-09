package com.aolda.ojakgyo.dto.kamis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KamisPriceDataDto {
    private String productclscode;
    private String caption;
    private List<KamisPriceItemMonthlyDto> item;
} 