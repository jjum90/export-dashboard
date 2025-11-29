package com.export.dashboard.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 수출 트렌드 분석 결과를 나타내는 Value Object
 * Java 21의 Record와 Pattern Matching을 활용한 불변 객체
 */
public record ExportTrend(
    List<MonthlyExportData> monthlyData,
    TrendDirection direction,
    BigDecimal averageGrowthRate,
    BigDecimal volatility
) {

    public ExportTrend {
        if (monthlyData == null || monthlyData.isEmpty()) {
            throw new IllegalArgumentException("월별 데이터는 필수입니다");
        }
        if (direction == null) {
            throw new IllegalArgumentException("트렌드 방향은 필수입니다");
        }
        if (averageGrowthRate == null) {
            throw new IllegalArgumentException("평균 성장률은 필수입니다");
        }
        if (volatility == null) {
            throw new IllegalArgumentException("변동성은 필수입니다");
        }
    }

    /**
     * 수출 통계 데이터로부터 트렌드 분석
     * Stream API와 Pattern Matching을 활용한 분석 로직
     */
    public static ExportTrend analyze(List<ExportStatistic> statistics, List<ExportPeriod> periods) {
        if (statistics == null || periods == null) {
            throw new IllegalArgumentException("분석 데이터는 필수입니다");
        }

        // 기간별 데이터 매핑
        var statisticsByPeriod = statistics.stream()
            .collect(Collectors.toMap(
                ExportStatistic::getPeriod,
                Function.identity(),
                (existing, replacement) -> existing
            ));

        // 월별 데이터 생성
        var monthlyData = periods.stream()
            .map(period -> createMonthlyData(period, statisticsByPeriod.get(period)))
            .toList();

        // 트렌드 분석
        var direction = analyzeTrendDirection(monthlyData);
        var averageGrowthRate = calculateAverageGrowthRate(monthlyData);
        var volatility = calculateVolatility(monthlyData);

        return new ExportTrend(monthlyData, direction, averageGrowthRate, volatility);
    }

    /**
     * 트렌드가 안정적인지 확인
     */
    public boolean isStable() {
        return switch (direction) {
            case STABLE -> true;
            case UPWARD, DOWNWARD -> volatility.compareTo(new BigDecimal("10")) <= 0;
            case VOLATILE -> false;
        };
    }

    /**
     * 트렌드가 성장 추세인지 확인
     */
    public boolean isGrowing() {
        return direction == TrendDirection.UPWARD &&
               averageGrowthRate.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 트렌드가 하락 추세인지 확인
     */
    public boolean isDeclining() {
        return direction == TrendDirection.DOWNWARD &&
               averageGrowthRate.compareTo(BigDecimal.ZERO) < 0;
    }

    private static MonthlyExportData createMonthlyData(ExportPeriod period, ExportStatistic statistic) {
        var value = statistic != null ? statistic.getExportValue() : Money.zero(java.util.Currency.getInstance("USD"));
        var growthRate = statistic != null && statistic.getGrowthRateYoy() != null
            ? statistic.getGrowthRateYoy()
            : Percentage.zero();

        return new MonthlyExportData(period, value, growthRate);
    }

    private static TrendDirection analyzeTrendDirection(List<MonthlyExportData> monthlyData) {
        if (monthlyData.size() < 2) {
            return TrendDirection.STABLE;
        }

        var growthRates = monthlyData.stream()
            .map(MonthlyExportData::growthRate)
            .map(Percentage::value)
            .toList();

        var positiveCount = growthRates.stream()
            .mapToInt(rate -> rate.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
            .sum();

        var negativeCount = growthRates.stream()
            .mapToInt(rate -> rate.compareTo(BigDecimal.ZERO) < 0 ? 1 : 0)
            .sum();

        var positiveRatio = (double) positiveCount / growthRates.size();
        var negativeRatio = (double) negativeCount / growthRates.size();

        int ratioIndex = (int) (positiveRatio * 10);
        if (ratioIndex <= 2) {
            return TrendDirection.DOWNWARD;
        } else if (ratioIndex <= 7) {
            return TrendDirection.VOLATILE;
        } else if (ratioIndex >= 8) {
            return TrendDirection.UPWARD;
        } else {
            return TrendDirection.STABLE;
        }
    }

    private static BigDecimal calculateAverageGrowthRate(List<MonthlyExportData> monthlyData) {
        var totalGrowthRate = monthlyData.stream()
            .map(MonthlyExportData::growthRate)
            .map(Percentage::value)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalGrowthRate.divide(
            BigDecimal.valueOf(monthlyData.size()),
            2,
            RoundingMode.HALF_UP
        );
    }

    private static BigDecimal calculateVolatility(List<MonthlyExportData> monthlyData) {
        if (monthlyData.size() < 2) {
            return BigDecimal.ZERO;
        }

        var growthRates = monthlyData.stream()
            .map(MonthlyExportData::growthRate)
            .map(Percentage::value)
            .toList();

        var mean = growthRates.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(growthRates.size()), 4, RoundingMode.HALF_UP);

        var variance = growthRates.stream()
            .map(rate -> rate.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(growthRates.size()), 4, RoundingMode.HALF_UP);

        // 표준편차 계산 (근사값)
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 월별 수출 데이터
     */
    public record MonthlyExportData(
        ExportPeriod period,
        Money exportValue,
        Percentage growthRate
    ) {
        public MonthlyExportData {
            if (period == null) {
                throw new IllegalArgumentException("기간은 필수입니다");
            }
            if (exportValue == null) {
                throw new IllegalArgumentException("수출 금액은 필수입니다");
            }
            if (growthRate == null) {
                throw new IllegalArgumentException("성장률은 필수입니다");
            }
        }
    }

    /**
     * 트렌드 방향을 나타내는 Enum
     * Java 17 호환을 위해 Enum 사용
     */
    public enum TrendDirection {
        UPWARD,
        DOWNWARD,
        STABLE,
        VOLATILE
    }
}