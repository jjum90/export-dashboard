package com.export.dashboard.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 지역별 수출 성과 비교 결과를 나타내는 Value Object
 * Java 21의 Record와 Pattern Matching을 활용한 지역 분석
 */
public record RegionalExportComparison(
    String region,
    ExportPeriod period,
    List<CountryPerformance> countryPerformances,
    RegionalSummary summary
) {

    public RegionalExportComparison {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("지역은 필수입니다");
        }
        if (period == null) {
            throw new IllegalArgumentException("기간은 필수입니다");
        }
        if (countryPerformances == null) {
            throw new IllegalArgumentException("국가별 성과는 필수입니다");
        }
        if (summary == null) {
            throw new IllegalArgumentException("지역 요약은 필수입니다");
        }
    }

    /**
     * 지역별 수출 통계로부터 비교 분석 생성
     */
    public static RegionalExportComparison create(String region, ExportPeriod period,
                                                List<ExportStatistic> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            return new RegionalExportComparison(
                region,
                period,
                List.of(),
                RegionalSummary.empty()
            );
        }

        // 국가별 통계 그룹핑
        var statisticsByCountry = statistics.stream()
            .collect(Collectors.groupingBy(ExportStatistic::getCountry));

        // 국가별 성과 계산
        var countryPerformances = statisticsByCountry.entrySet().stream()
            .map(entry -> calculateCountryPerformance(entry.getKey(), entry.getValue()))
            .sorted((p1, p2) -> p2.totalExportValue().amount()
                .compareTo(p1.totalExportValue().amount()))
            .toList();

        // 지역 요약 계산
        var summary = calculateRegionalSummary(countryPerformances, statistics);

        return new RegionalExportComparison(region, period, countryPerformances, summary);
    }

    /**
     * 특정 국가의 성과 조회
     */
    public CountryPerformance getPerformanceByCountry(CountryCode countryCode) {
        return countryPerformances.stream()
            .filter(performance -> performance.country().getCountryCode().equals(countryCode))
            .findFirst()
            .orElse(null);
    }

    /**
     * 상위 N개 수출 국가 조회
     */
    public List<CountryPerformance> getTopPerformingCountries(int limit) {
        return countryPerformances.stream()
            .limit(Math.max(0, limit))
            .toList();
    }

    /**
     * 지역 내 시장 점유율 계산
     */
    public Map<CountryCode, Percentage> getMarketShareByCountry() {
        var totalValue = summary.totalExportValue();

        return countryPerformances.stream()
            .collect(Collectors.toMap(
                performance -> performance.country().getCountryCode(),
                performance -> totalValue.isZero()
                    ? Percentage.zero()
                    : Percentage.calculate(
                        performance.totalExportValue().amount(),
                        totalValue.amount()
                    )
            ));
    }

    /**
     * 국가별 성과 계산
     */
    private static CountryPerformance calculateCountryPerformance(Country country,
                                                                List<ExportStatistic> statistics) {
        var totalValue = statistics.stream()
            .map(ExportStatistic::getExportValue)
            .reduce(Money.zero(statistics.get(0).getExportValue().currency()), Money::add);

        var productCount = statistics.stream()
            .map(ExportStatistic::getProductCategory)
            .distinct()
            .count();

        var averageGrowthRate = statistics.stream()
            .map(ExportStatistic::getGrowthRateYoy)
            .filter(rate -> rate != null && !rate.isZero())
            .map(Percentage::value)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(statistics.size()), 2, RoundingMode.HALF_UP);

        return new CountryPerformance(
            country,
            totalValue,
            (int) productCount,
            Percentage.of(averageGrowthRate)
        );
    }

    /**
     * 지역 요약 계산
     */
    private static RegionalSummary calculateRegionalSummary(List<CountryPerformance> performances,
                                                          List<ExportStatistic> allStatistics) {
        if (performances.isEmpty()) {
            return RegionalSummary.empty();
        }

        var totalValue = performances.stream()
            .map(CountryPerformance::totalExportValue)
            .reduce(Money.zero(performances.get(0).totalExportValue().currency()), Money::add);

        var countryCount = performances.size();

        var totalProductCount = allStatistics.stream()
            .map(ExportStatistic::getProductCategory)
            .distinct()
            .count();

        var averageGrowthRate = performances.stream()
            .map(CountryPerformance::averageGrowthRate)
            .map(Percentage::value)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(countryCount), 2, RoundingMode.HALF_UP);

        return new RegionalSummary(
            totalValue,
            countryCount,
            (int) totalProductCount,
            Percentage.of(averageGrowthRate)
        );
    }

    /**
     * 국가별 성과
     */
    public record CountryPerformance(
        Country country,
        Money totalExportValue,
        int productCount,
        Percentage averageGrowthRate
    ) {
        public CountryPerformance {
            if (country == null) {
                throw new IllegalArgumentException("국가는 필수입니다");
            }
            if (totalExportValue == null) {
                throw new IllegalArgumentException("총 수출 금액은 필수입니다");
            }
            if (productCount < 0) {
                throw new IllegalArgumentException("상품 수는 0 이상이어야 합니다");
            }
            if (averageGrowthRate == null) {
                throw new IllegalArgumentException("평균 성장률은 필수입니다");
            }
        }

        public boolean isTopPerformer(Money regionTotal) {
            if (regionTotal.isZero()) {
                return false;
            }
            var share = Percentage.calculate(totalExportValue.amount(), regionTotal.amount());
            return share.value().compareTo(new BigDecimal("30")) >= 0; // 30% 이상이면 주요 수출국
        }

        public boolean hasPositiveGrowth() {
            return averageGrowthRate.value().compareTo(BigDecimal.ZERO) > 0;
        }
    }

    /**
     * 지역 요약
     */
    public record RegionalSummary(
        Money totalExportValue,
        int countryCount,
        int totalProductCount,
        Percentage averageGrowthRate
    ) {
        public RegionalSummary {
            if (totalExportValue == null) {
                throw new IllegalArgumentException("총 수출 금액은 필수입니다");
            }
            if (countryCount < 0) {
                throw new IllegalArgumentException("국가 수는 0 이상이어야 합니다");
            }
            if (totalProductCount < 0) {
                throw new IllegalArgumentException("상품 수는 0 이상이어야 합니다");
            }
            if (averageGrowthRate == null) {
                throw new IllegalArgumentException("평균 성장률은 필수입니다");
            }
        }

        public static RegionalSummary empty() {
            return new RegionalSummary(
                Money.zero(java.util.Currency.getInstance("USD")),
                0,
                0,
                Percentage.zero()
            );
        }

        public boolean isEmpty() {
            return countryCount == 0 && totalProductCount == 0;
        }

        public boolean isGrowingRegion() {
            return averageGrowthRate.value().compareTo(BigDecimal.ZERO) > 0;
        }
    }
}