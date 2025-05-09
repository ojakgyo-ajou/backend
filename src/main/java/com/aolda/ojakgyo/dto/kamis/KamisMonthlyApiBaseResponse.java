package com.aolda.ojakgyo.dto.kamis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KamisMonthlyApiBaseResponse {
    private List<KamisConditionDto> condition;
    @JsonProperty("error_code")
    private String errorCode;
    private KamisPriceDataDto price;
} 