package com.export.dashboard.domain.service;

import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.ExportStatisticRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 수출 통계 관련 도메인 서비스
 * 복잡한 비즈니스 로직과 여러 Aggregate 간의 협력을 처리
 */
@Service
public class ExportStatisticDomainService {

    private final ExportStatisticRepository exportStatisticRepository;

    public ExportStatisticDomainService(ExportStatisticRepository exportStatisticRepository) {
        this.exportStatisticRepository = exportStatisticRepository;
    }

    /**
     * 전년 동월 대비 성장률 계산 및 업데이트
     */
    public void calculateAndUpdateGrowthRate(ExportStatistic currentStatistic) {
        ExportPeriod previousYearPeriod = currentStatistic.getPeriod().sameMonthPreviousYear();

        Optional<ExportStatistic> previousYearStatistic = exportStatisticRepository
            .findByCountryAndProductAndPeriod(
                currentStatistic.getCountry(),
                currentStatistic.getProductCategory(),
                previousYearPeriod
            );

        if (previousYearStatistic.isPresent()) {
            Money previousYearValue = previousYearStatistic.get().getExportValue();
            currentStatistic.calculateGrowthRate(previousYearValue);
        } else {
            currentStatistic.calculateGrowthRate(Money.zero(currentStatistic.getExportValue().currency()));
        }
    }

    /**
     * 시장 점유율 계산 및 업데이트
     */
    public void calculateAndUpdateMarketShare(ExportStatistic statistic) {
        Money totalMarketValue = exportStatisticRepository.getTotalExportValueByYear(
            statistic.getPeriod().year()
        );

        statistic.updateMarketShare(totalMarketValue);
    }

    /**
     * 특정 국가의 년도별 수출 성과 분석
     */
    public CountryExportPerformance analyzeCountryPerformance(Country country, Integer year) {
        List<ExportStatistic> statistics = exportStatisticRepository
            .findByCountryAndYear(country, year);

        if (statistics.isEmpty()) {
            return new CountryExportPerformance(
                country.getCountryCode(),
                year,
                Money.zero(java.util.Currency.getInstance("USD")),
                Percentage.zero(),
                0
            );
        }

        Money totalValue = statistics.stream()
            .map(ExportStatistic::getExportValue)
            .reduce(Money.zero(java.util.Currency.getInstance("USD")), Money::add);

        Money previousYearTotal = exportStatisticRepository.getTotalExportValueByYear(year - 1);
        Percentage growthRate = Percentage.calculate(
            totalValue.subtract(previousYearTotal).amount(),
            previousYearTotal.amount()
        );

        int productCount = (int) statistics.stream()
            .map(ExportStatistic::getProductCategory)
            .distinct()
            .count();

        return new CountryExportPerformance(
            country.getCountryCode(),
            year,
            totalValue,
            growthRate,
            productCount
        );
    }

    /**
     * 특정 상품의 년도별 수출 성과 분석
     */
    public ProductExportPerformance analyzeProductPerformance(ProductCategory productCategory, Integer year) {
        List<ExportStatistic> statistics = exportStatisticRepository
            .findByProductCategoryAndYear(productCategory, year);

        if (statistics.isEmpty()) {
            return new ProductExportPerformance(
                productCategory.getHsCode(),
                year,
                Money.zero(java.util.Currency.getInstance("USD")),
                Percentage.zero(),
                0
            );
        }

        Money totalValue = statistics.stream()
            .map(ExportStatistic::getExportValue)
            .reduce(Money.zero(java.util.Currency.getInstance("USD")), Money::add);

        Money previousYearTotal = exportStatisticRepository.getTotalExportValueByYear(year - 1);
        Percentage growthRate = Percentage.calculate(
            totalValue.subtract(previousYearTotal).amount(),
            previousYearTotal.amount()
        );

        int countryCount = (int) statistics.stream()
            .map(ExportStatistic::getCountry)
            .distinct()
            .count();

        return new ProductExportPerformance(
            productCategory.getHsCode(),
            year,
            totalValue,
            growthRate,
            countryCount
        );
    }

    /**
     * 수출 데이터 일관성 검증
     */
    public void validateExportDataConsistency(ExportStatistic statistic) {
        // 중복 데이터 검증
        Optional<ExportStatistic> existing = exportStatisticRepository
            .findByCountryAndProductAndPeriod(
                statistic.getCountry(),
                statistic.getProductCategory(),
                statistic.getPeriod()
            );

        if (existing.isPresent() && !existing.get().getId().equals(statistic.getId())) {
            throw new IllegalStateException(
                String.format("동일한 국가(%s), 상품(%s), 기간(%s)의 수출 통계가 이미 존재합니다.",
                    statistic.getCountry().getCountryCode(),
                    statistic.getProductCategory().getHsCode(),
                    statistic.getPeriod())
            );
        }
    }

    /**
     * 수출 통계 배치 업데이트
     */
    public void batchUpdateGrowthRatesAndMarketShares(List<ExportStatistic> statistics) {
        statistics.forEach(this::calculateAndUpdateGrowthRate);
        statistics.forEach(this::calculateAndUpdateMarketShare);
    }

    /**
     * 국가별 수출 성과 분석 결과
     */
    public record CountryExportPerformance(
        CountryCode countryCode,
        Integer year,
        Money totalExportValue,
        Percentage growthRate,
        Integer exportedProductCount
    ) {}

    /**
     * 상품별 수출 성과 분석 결과
     */
    public record ProductExportPerformance(
        HsCode hsCode,
        Integer year,
        Money totalExportValue,
        Percentage growthRate,
        Integer exportingCountryCount
    ) {}
}