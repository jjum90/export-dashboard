package com.export.dashboard.application.dto;

import com.export.dashboard.domain.service.ExportAnalyticsDomainService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 대시보드 요약 정보 응답 DTO
 */
public record DashboardSummaryResponse(
    Integer year,
    BigDecimal totalExportValue,
    String currency,
    BigDecimal yearOverYearGrowth,
    Integer totalCountries,
    Integer totalProducts,
    List<CountryExportSummaryResponse> topCountries,
    List<ProductExportSummaryResponse> topProducts,
    List<MonthlyTrendResponse> monthlyTrends
) {
    public static DashboardSummaryResponse from(ExportAnalyticsDomainService.DashboardSummary summary) {
        return new DashboardSummaryResponse(
            summary.year(),
            summary.totalExportValue().amount(),
            summary.totalExportValue().currency().getCurrencyCode(),
            summary.yearOverYearGrowth().value(),
            summary.totalCountries(),
            summary.totalProducts(),
            summary.topCountries().stream()
                .map(CountryExportSummaryResponse::from)
                .toList(),
            summary.topProducts().stream()
                .map(ProductExportSummaryResponse::from)
                .toList(),
            summary.monthlyTrends().stream()
                .map(MonthlyTrendResponse::from)
                .toList()
        );
    }

    /**
     * 국가별 수출 요약 정보
     */
    public record CountryExportSummaryResponse(
        String countryCode,
        String countryName,
        BigDecimal totalValue,
        BigDecimal marketShare
    ) {
        public static CountryExportSummaryResponse from(
            com.export.dashboard.domain.repository.ExportStatisticRepository.CountryExportSummary summary) {
            return new CountryExportSummaryResponse(
                summary.countryCode().value(),
                summary.countryName(),
                summary.totalValue().amount(),
                summary.marketShare().value()
            );
        }
    }

    /**
     * 상품별 수출 요약 정보
     */
    public record ProductExportSummaryResponse(
        String hsCode,
        String productName,
        BigDecimal totalValue,
        BigDecimal marketShare
    ) {
        public static ProductExportSummaryResponse from(
            com.export.dashboard.domain.repository.ExportStatisticRepository.ProductExportSummary summary) {
            return new ProductExportSummaryResponse(
                summary.hsCode().value(),
                summary.productName(),
                summary.totalValue().amount(),
                summary.marketShare().value()
            );
        }
    }

    /**
     * 월별 트렌드 정보
     */
    public record MonthlyTrendResponse(
        Integer year,
        Integer month,
        BigDecimal totalValue
    ) {
        public static MonthlyTrendResponse from(
            com.export.dashboard.domain.repository.ExportStatisticRepository.MonthlyExportTrend trend) {
            return new MonthlyTrendResponse(
                trend.period().year(),
                trend.period().month(),
                trend.totalValue().amount()
            );
        }
    }
}