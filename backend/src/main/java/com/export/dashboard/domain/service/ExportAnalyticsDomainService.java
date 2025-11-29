package com.export.dashboard.domain.service;

import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.ExportStatisticRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 수출 분석 관련 도메인 서비스
 * 고급 분석 로직과 통계 계산을 처리
 */
@Service
public class ExportAnalyticsDomainService {

    private final ExportStatisticRepository exportStatisticRepository;

    public ExportAnalyticsDomainService(ExportStatisticRepository exportStatisticRepository) {
        this.exportStatisticRepository = exportStatisticRepository;
    }

    /**
     * 종합 대시보드 요약 정보 생성
     */
    public DashboardSummary generateDashboardSummary(Integer year) {
        // 총 수출액
        Money totalExportValue = exportStatisticRepository.getTotalExportValueByYear(year);

        // 전년 대비 성장률 (역성장 시 음수 퍼센티지 허용)
        Money previousYearValue = exportStatisticRepository.getTotalExportValueByYear(year - 1);
        BigDecimal difference = totalExportValue.amount().subtract(previousYearValue.amount());
        Percentage yearOverYearGrowth = Percentage.calculate(
            difference,
            previousYearValue.amount()
        );

        // 수출 국가 및 상품 수
        Long countryCount = exportStatisticRepository.countDistinctCountriesByYear(year);
        Long productCount = exportStatisticRepository.countDistinctProductsByYear(year);

        // 상위 수출 국가
        List<ExportStatisticRepository.CountryExportSummary> topCountries =
            exportStatisticRepository.getTopExportCountriesByYear(year, 10);

        // 상위 수출 상품
        List<ExportStatisticRepository.ProductExportSummary> topProducts =
            exportStatisticRepository.getTopExportProductsByYear(year, 10);

        // 월별 트렌드
        List<ExportStatisticRepository.MonthlyExportTrend> monthlyTrends =
            exportStatisticRepository.getMonthlyExportTrend(year);

        return new DashboardSummary(
            year,
            totalExportValue,
            yearOverYearGrowth,
            countryCount.intValue(),
            productCount.intValue(),
            topCountries,
            topProducts,
            monthlyTrends
        );
    }

    /**
     * 수출 다양성 지수 계산 (Herfindahl-Hirschman Index 변형)
     */
    public BigDecimal calculateExportDiversityIndex(Integer year) {
        Money totalValue = exportStatisticRepository.getTotalExportValueByYear(year);

        if (totalValue.isZero()) {
            return BigDecimal.ZERO;
        }

        List<ExportStatisticRepository.ProductExportSummary> productSummaries =
            exportStatisticRepository.getTopExportProductsByYear(year, Integer.MAX_VALUE);

        BigDecimal hhi = productSummaries.stream()
            .map(summary -> {
                BigDecimal share = summary.totalValue().amount()
                    .divide(totalValue.amount(), 4, RoundingMode.HALF_UP);
                return share.multiply(share);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 정규화된 다양성 지수 (1 - HHI)
        return BigDecimal.ONE.subtract(hhi);
    }

    /**
     * 지역별 수출 집중도 분석
     */
    public RegionalConcentration analyzeRegionalConcentration(Integer year) {
        List<ExportStatisticRepository.CountryExportSummary> countrySummaries =
            exportStatisticRepository.getTopExportCountriesByYear(year, Integer.MAX_VALUE);

        Money totalValue = exportStatisticRepository.getTotalExportValueByYear(year);

        // 상위 5개국 집중도
        Money top5Value = countrySummaries.stream()
            .limit(5)
            .map(ExportStatisticRepository.CountryExportSummary::totalValue)
            .reduce(Money.zero(totalValue.currency()), Money::add);

        Percentage top5Concentration = Percentage.calculate(top5Value.amount(), totalValue.amount());

        // 상위 10개국 집중도
        Money top10Value = countrySummaries.stream()
            .limit(10)
            .map(ExportStatisticRepository.CountryExportSummary::totalValue)
            .reduce(Money.zero(totalValue.currency()), Money::add);

        Percentage top10Concentration = Percentage.calculate(top10Value.amount(), totalValue.amount());

        return new RegionalConcentration(
            year,
            totalValue,
            top5Concentration,
            top10Concentration,
            countrySummaries.size()
        );
    }

    /**
     * 계절성 분석 (월별 수출 패턴)
     */
    public SeasonalityAnalysis analyzeSeasonality(Integer year) {
        List<ExportStatisticRepository.MonthlyExportTrend> monthlyTrends =
            exportStatisticRepository.getMonthlyExportTrend(year);

        if (monthlyTrends.isEmpty()) {
            return new SeasonalityAnalysis(year, BigDecimal.ZERO, 1, 1, false);
        }

        // 월별 평균 계산
        BigDecimal average = monthlyTrends.stream()
            .map(trend -> trend.totalValue().amount())
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(monthlyTrends.size()), 2, RoundingMode.HALF_UP);

        // 변동 계수 계산
        BigDecimal variance = monthlyTrends.stream()
            .map(trend -> {
                BigDecimal diff = trend.totalValue().amount().subtract(average);
                return diff.multiply(diff);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(monthlyTrends.size()), 4, RoundingMode.HALF_UP);

        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal coefficientOfVariation = average.compareTo(BigDecimal.ZERO) > 0 ?
            standardDeviation.divide(average, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 최고/최저 월 찾기
        ExportStatisticRepository.MonthlyExportTrend maxMonth = monthlyTrends.stream()
            .max((t1, t2) -> t1.totalValue().amount().compareTo(t2.totalValue().amount()))
            .orElse(monthlyTrends.get(0));

        ExportStatisticRepository.MonthlyExportTrend minMonth = monthlyTrends.stream()
            .min((t1, t2) -> t1.totalValue().amount().compareTo(t2.totalValue().amount()))
            .orElse(monthlyTrends.get(0));

        // 계절성 존재 여부 (변동계수가 0.1 이상이면 계절성 있음으로 판단)
        boolean hasSeasonality = coefficientOfVariation.compareTo(BigDecimal.valueOf(0.1)) >= 0;

        return new SeasonalityAnalysis(
            year,
            coefficientOfVariation,
            maxMonth.period().month(),
            minMonth.period().month(),
            hasSeasonality
        );
    }

    /**
     * 수출 성장 추세 분석
     */
    public GrowthTrendAnalysis analyzeGrowthTrend(Integer startYear, Integer endYear) {
        List<ExportStatisticRepository.YearlyExportTrend> yearlyTrends =
            exportStatisticRepository.getYearlyExportTrend(startYear, endYear);

        if (yearlyTrends.size() < 2) {
            return new GrowthTrendAnalysis(startYear, endYear, BigDecimal.ZERO, "insufficient_data");
        }

        // 연평균 성장률 계산 (CAGR - Compound Annual Growth Rate)
        Money firstYearValue = yearlyTrends.get(0).totalValue();
        Money lastYearValue = yearlyTrends.get(yearlyTrends.size() - 1).totalValue();
        int years = endYear - startYear;

        BigDecimal cagr = BigDecimal.ZERO;
        if (firstYearValue.isPositive() && years > 0) {
            double cagrValue = Math.pow(
                lastYearValue.amount().divide(firstYearValue.amount(), 4, RoundingMode.HALF_UP).doubleValue(),
                1.0 / years
            ) - 1.0;
            cagr = BigDecimal.valueOf(cagrValue * 100).setScale(2, RoundingMode.HALF_UP);
        }

        // 추세 분류
        String trendType;
        if (cagr.compareTo(BigDecimal.valueOf(5)) > 0) {
            trendType = "high_growth";
        } else if (cagr.compareTo(BigDecimal.ZERO) > 0) {
            trendType = "moderate_growth";
        } else if (cagr.compareTo(BigDecimal.valueOf(-5)) > 0) {
            trendType = "stable";
        } else {
            trendType = "declining";
        }

        return new GrowthTrendAnalysis(startYear, endYear, cagr, trendType);
    }

    // Record classes for analysis results
    public record DashboardSummary(
        Integer year,
        Money totalExportValue,
        Percentage yearOverYearGrowth,
        Integer totalCountries,
        Integer totalProducts,
        List<ExportStatisticRepository.CountryExportSummary> topCountries,
        List<ExportStatisticRepository.ProductExportSummary> topProducts,
        List<ExportStatisticRepository.MonthlyExportTrend> monthlyTrends
    ) {}

    public record RegionalConcentration(
        Integer year,
        Money totalValue,
        Percentage top5Concentration,
        Percentage top10Concentration,
        Integer totalCountries
    ) {}

    public record SeasonalityAnalysis(
        Integer year,
        BigDecimal coefficientOfVariation,
        Integer peakMonth,
        Integer troughMonth,
        Boolean hasSeasonality
    ) {}

    public record GrowthTrendAnalysis(
        Integer startYear,
        Integer endYear,
        BigDecimal compoundAnnualGrowthRate,
        String trendType
    ) {}
}