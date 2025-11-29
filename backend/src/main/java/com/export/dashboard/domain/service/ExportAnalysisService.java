package com.export.dashboard.domain.service;

import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.ExportStatisticRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * 수출 분석 도메인 서비스
 * Java 21의 Pattern Matching과 Record Pattern을 활용한 복잡한 비즈니스 로직 처리
 */
public class ExportAnalysisService {

    private final ExportStatisticRepository exportStatisticRepository;

    public ExportAnalysisService(ExportStatisticRepository exportStatisticRepository) {
        this.exportStatisticRepository = Objects.requireNonNull(
            exportStatisticRepository, "수출 통계 리포지토리는 필수입니다");
    }

    /**
     * 전년 동월 대비 성장률 계산
     * Java 21의 Pattern Matching을 활용한 비즈니스 로직
     */
    public Percentage calculateGrowthRate(ExportStatistic currentStatistic) {
        validateNotNull(currentStatistic, "수출 통계는 필수입니다");
        validateActiveEntities(currentStatistic);

        var previousPeriod = currentStatistic.getPeriod().sameMonthPreviousYear();
        var previousStatistics = exportStatisticRepository.findAllByCountryAndProductAndPeriod(
            currentStatistic.getCountry(),
            currentStatistic.getProductCategory(),
            previousPeriod
        );

        return switch (previousStatistics.size()) {
            case 0 -> null; // 이전 년도 데이터 없음
            case 1 -> {
                var previousStat = previousStatistics.get(0);
                yield calculateGrowthRateInternal(
                    currentStatistic.getExportValue(),
                    previousStat.getExportValue()
                );
            }
            default -> throw new IllegalStateException(
                "동일 기간에 중복된 수출 통계가 존재합니다: " + previousStatistics.size() + "개");
        };
    }

    /**
     * 시장 점유율 계산
     * Record Pattern을 활용한 데이터 분해와 계산
     */
    public Percentage calculateMarketShare(ExportStatistic targetStatistic) {
        validateNotNull(targetStatistic, "수출 통계는 필수입니다");
        validateActiveEntities(targetStatistic);

        var allStatistics = exportStatisticRepository.findByProductAndPeriod(
            targetStatistic.getProductCategory(),
            targetStatistic.getPeriod()
        );

        var totalMarketValue = allStatistics.stream()
            .map(ExportStatistic::getExportValue)
            .reduce(Money.zero(targetStatistic.getExportValue().currency()), Money::add);

        return totalMarketValue.isZero()
            ? Percentage.zero()
            : Percentage.calculate(
                targetStatistic.getExportValue().amount(),
                totalMarketValue.amount()
            );
    }

    /**
     * 특정 국가의 상위 N개 수출 상품 조회
     * Stream API와 함께 사용되는 최신 Java 기능 활용
     */
    public List<ExportStatistic> getTopExportProducts(Country country, ExportPeriod period, int limit) {
        validateNotNull(country, "국가는 필수입니다");
        validateNotNull(period, "기간은 필수입니다");
        validatePositive(limit, "조회 개수는 양수여야 합니다");
        validateActiveCountry(country);

        return exportStatisticRepository.findByCountryAndPeriodOrderByValueDesc(country, period)
            .stream()
            .limit(limit)
            .toList();
    }

    /**
     * 수출 트렌드 분석
     * Pattern Matching을 활용한 트렌드 분석 로직
     */
    public ExportTrend analyzeExportTrend(Country country, ProductCategory product, int monthsBack) {
        validateNotNull(country, "국가는 필수입니다");
        validateNotNull(product, "상품 카테고리는 필수입니다");
        validatePositive(monthsBack, "분석 기간은 양수여야 합니다");
        validateActiveEntities(country, product);

        var currentDate = LocalDate.now();
        var periods = IntStream.range(0, monthsBack)
            .mapToObj(i -> currentDate.minusMonths(i))
            .map(date -> ExportPeriod.of(date.getYear(), date.getMonthValue()))
            .toList();

        var statistics = exportStatisticRepository.findByCountryAndProductInPeriods(
            country, product, periods);

        return ExportTrend.analyze(statistics, periods);
    }

    /**
     * 지역별 수출 성과 비교
     * Sealed Class와 Pattern Matching을 활용한 지역 분석
     */
    public RegionalExportComparison compareRegionalPerformance(String region, ExportPeriod period) {
        validateNotNull(region, "지역은 필수입니다");
        validateNotNull(period, "기간은 필수입니다");

        var regionalStatistics = exportStatisticRepository.findByRegionAndPeriod(region, period);

        return RegionalExportComparison.create(region, period, regionalStatistics);
    }

    // Private helper methods
    private Percentage calculateGrowthRateInternal(Money current, Money previous) {
        if (previous.isZero()) {
            return current.isZero() ? Percentage.zero() : Percentage.of(new BigDecimal("100"));
        }

        var difference = current.subtract(previous);
        return Percentage.calculate(difference.amount(), previous.amount());
    }

    private void validateNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePositive(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateActiveEntities(ExportStatistic statistic) {
        validateActiveCountry(statistic.getCountry());
        validateActiveProduct(statistic.getProductCategory());
    }

    private void validateActiveEntities(Country country, ProductCategory product) {
        validateActiveCountry(country);
        validateActiveProduct(product);
    }

    private void validateActiveCountry(Country country) {
        if (!country.isActive()) {
            throw new IllegalArgumentException("비활성화된 국가입니다: " + country.getCountryCode());
        }
    }

    private void validateActiveProduct(ProductCategory product) {
        if (!product.isActive()) {
            throw new IllegalArgumentException("비활성화된 상품 카테고리입니다: " + product.getHsCode());
        }
    }
}