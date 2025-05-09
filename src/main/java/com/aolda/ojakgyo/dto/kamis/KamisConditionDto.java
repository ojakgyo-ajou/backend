package com.aolda.ojakgyo.dto.kamis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KamisConditionDto {
    @JsonProperty("p_cert_id") private String pCertId;
    @JsonProperty("p_cert_key") private String pCertKey;
    @JsonProperty("p_returntype") private String pReturntype;
    @JsonProperty("p_yyyy") private String pYyyy;
    @JsonProperty("p_period") private String pPeriod;
    @JsonProperty("p_itemcategorycode") private String pItemcategorycode;
    @JsonProperty("p_itemcode") private String pItemcode;
    @JsonProperty("p_kindcode") private String pKindcode;
    @JsonProperty("p_graderank") private String pGraderank;
    @JsonProperty("p_countycode") private String pCountycode;
} 