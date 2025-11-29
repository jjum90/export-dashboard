package com.export.dashboard.application.dto;

import com.export.dashboard.domain.model.ExportStatistic;

import java.math.BigDecimal;

/**
 * 수출 통계 정보 응답 DTO
 */
public record ExportStatisticResponse(
    Long id,
    CountryResponse country,
    ProductCategoryResponse productCategory,
    Integer year,
    Integer month,
    BigDecimal exportValueUsd,
    String currency,
    BigDecimal exportWeightKg,
    BigDecimal exportQuantity,
    String quantityUnit,
    BigDecimal growthRateYoy,
    BigDecimal marketShare
) {
    public static ExportStatisticResponse from(ExportStatistic statistic) {
        return new ExportStatisticResponse(
            statistic.getId(),
            CountryResponse.from(statistic.getCountry()),
            ProductCategoryResponse.from(statistic.getProductCategory()),
            statistic.getPeriod().year(),
            statistic.getPeriod().month(),
            statistic.getExportValue().amount(),
            statistic.getExportValue().currency().getCurrencyCode(),
            statistic.getExportWeightKg(),
            statistic.getExportQuantity(),
            statistic.getQuantityUnit(),
            statistic.getGrowthRateYoy() != null ? statistic.getGrowthRateYoy().value() : null,
            statistic.getMarketShare() != null ? statistic.getMarketShare().value() : null
        );
    }
}