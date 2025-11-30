package com.export.dashboard.infrastructure.repository;

import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.ExportStatisticRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

/**
 * ExportStatistic Repository 구현체
 * 도메인 Repository 인터페이스를 JPA Repository로 구현
 */
@Repository
public class ExportStatisticRepositoryImpl implements ExportStatisticRepository {

    private final JpaExportStatisticRepository jpaExportStatisticRepository;

    public ExportStatisticRepositoryImpl(JpaExportStatisticRepository jpaExportStatisticRepository) {
        this.jpaExportStatisticRepository = jpaExportStatisticRepository;
    }

    @Override
    public ExportStatistic save(ExportStatistic exportStatistic) {
        return jpaExportStatisticRepository.save(exportStatistic);
    }

    @Override
    public Optional<ExportStatistic> findById(Long id) {
        return jpaExportStatisticRepository.findById(id);
    }

    @Override
    public Optional<ExportStatistic> findByCountryAndProductAndPeriod(
            Country country, ProductCategory productCategory, ExportPeriod period) {
        return jpaExportStatisticRepository.findByCountryAndProductAndPeriod(
            country, productCategory, period.year(), period.month());
    }

    @Override
    public Optional<ExportStatistic> findByCountryAndProductCategoryAndPeriod(
            Country country, ProductCategory productCategory, Integer year, Integer month) {
        return jpaExportStatisticRepository.findByCountryAndProductAndPeriod(
            country, productCategory, year, month);
    }

    @Override
    public List<ExportStatistic> findAllByCountryAndProductAndPeriod(
            Country country, ProductCategory productCategory, ExportPeriod period) {
        return jpaExportStatisticRepository.findAllByCountryAndProductAndPeriod(
            country, productCategory, period.year(), period.month());
    }

    @Override
    public List<ExportStatistic> findByProductAndPeriod(ProductCategory productCategory, ExportPeriod period) {
        return jpaExportStatisticRepository.findByProductAndPeriod(
            productCategory, period.year(), period.month());
    }

    @Override
    public List<ExportStatistic> findByCountryAndPeriodOrderByValueDesc(Country country, ExportPeriod period) {
        return jpaExportStatisticRepository.findByCountryAndPeriodOrderByValueDesc(
            country, period.year(), period.month());
    }

    @Override
    public List<ExportStatistic> findByCountryAndProductInPeriods(
            Country country, ProductCategory productCategory, List<ExportPeriod> periods) {
        // For simplicity, we'll query each period separately and combine results
        return periods.stream()
                .flatMap(period -> jpaExportStatisticRepository
                    .findAllByCountryAndProductAndPeriod(country, productCategory, period.year(), period.month())
                    .stream())
                .toList();
    }

    @Override
    public List<ExportStatistic> findByRegionAndPeriod(String region, ExportPeriod period) {
        return jpaExportStatisticRepository.findByRegionAndPeriod(region, period.year(), period.month());
    }

    @Override
    public List<ExportStatistic> findByYear(Integer year) {
        return jpaExportStatisticRepository.findByYear(year);
    }

    @Override
    public List<ExportStatistic> findByCountryAndYear(Country country, Integer year) {
        return jpaExportStatisticRepository.findByCountryAndYear(country, year);
    }

    @Override
    public List<ExportStatistic> findByProductCategoryAndYear(ProductCategory productCategory, Integer year) {
        return jpaExportStatisticRepository.findByProductCategoryAndYear(productCategory, year);
    }

    @Override
    public List<ExportStatistic> findByPeriodBetween(ExportPeriod startPeriod, ExportPeriod endPeriod) {
        return jpaExportStatisticRepository.findByPeriodBetween(
            startPeriod.year(), startPeriod.month(),
            endPeriod.year(), endPeriod.month());
    }

    @Override
    public Page<ExportStatistic> findAll(Pageable pageable) {
        return jpaExportStatisticRepository.findAll(pageable);
    }

    @Override
    public Money getTotalExportValueByYear(Integer year) {
        BigDecimal totalValue = jpaExportStatisticRepository.getTotalExportValueByYear(year);
        return Money.usd(totalValue != null ? totalValue : BigDecimal.ZERO);
    }

    @Override
    public Long countDistinctCountriesByYear(Integer year) {
        return jpaExportStatisticRepository.countDistinctCountriesByYear(year);
    }

    @Override
    public Long countDistinctProductsByYear(Integer year) {
        return jpaExportStatisticRepository.countDistinctProductsByYear(year);
    }

    @Override
    public List<CountryExportSummary> getTopExportCountriesByYear(Integer year, int limit) {
        List<Object[]> results = jpaExportStatisticRepository.getTopExportCountriesByYear(year);
        Money totalValue = getTotalExportValueByYear(year);

        return results.stream()
            .limit(limit)
            .map(row -> {
                String countryCode = (String) row[0];
                String countryName = (String) row[1];
                BigDecimal value = (BigDecimal) row[2];

                Money exportValue = Money.usd(value);
                Percentage marketShare = Percentage.calculate(value, totalValue.amount());

                return new CountryExportSummary(
                    CountryCode.from(countryCode),
                    countryName,
                    exportValue,
                    marketShare
                );
            })
            .toList();
    }

    @Override
    public List<ProductExportSummary> getTopExportProductsByYear(Integer year, int limit) {
        List<Object[]> results = jpaExportStatisticRepository.getTopExportProductsByYear(year);
        Money totalValue = getTotalExportValueByYear(year);

        return results.stream()
            .limit(limit)
            .map(row -> {
                String hsCode = (String) row[0];
                String productName = (String) row[1];
                BigDecimal value = (BigDecimal) row[2];

                Money exportValue = Money.usd(value);
                Percentage marketShare = Percentage.calculate(value, totalValue.amount());

                // HS 코드 레벨을 추정 (실제로는 ProductCategory에서 가져와야 함)
                int level = estimateHsCodeLevel(hsCode);

                return new ProductExportSummary(
                    HsCode.from(hsCode, level),
                    productName,
                    exportValue,
                    marketShare
                );
            })
            .toList();
    }

    @Override
    public List<MonthlyExportTrend> getMonthlyExportTrend(Integer year) {
        List<Object[]> results = jpaExportStatisticRepository.getMonthlyExportTrend(year);

        return results.stream()
            .map(row -> {
                Integer trendYear = (Integer) row[0];
                Integer month = (Integer) row[1];
                BigDecimal value = (BigDecimal) row[2];

                return new MonthlyExportTrend(
                    ExportPeriod.of(trendYear, month),
                    Money.usd(value)
                );
            })
            .toList();
    }

    @Override
    public List<YearlyExportTrend> getYearlyExportTrend(Integer startYear, Integer endYear) {
        List<Object[]> results = jpaExportStatisticRepository.getYearlyExportTrend(startYear, endYear);

        return results.stream()
            .map(row -> {
                Integer year = (Integer) row[0];
                BigDecimal value = (BigDecimal) row[1];

                // 성장률 계산 (이전 년도 대비)
                Percentage growthRate = Percentage.zero();
                if (year > startYear) {
                    Money previousYearValue = getTotalExportValueByYear(year - 1);
                    Money currentValue = Money.usd(value);
                    if (previousYearValue.isPositive()) {
                        growthRate = Percentage.calculate(
                            currentValue.subtract(previousYearValue).amount(),
                            previousYearValue.amount()
                        );
                    }
                }

                return new YearlyExportTrend(
                    year,
                    Money.usd(value),
                    growthRate
                );
            })
            .toList();
    }

    @Override
    public List<Integer> findAllYears() {
        return jpaExportStatisticRepository.findAllYears();
    }

    @Override
    public void delete(ExportStatistic exportStatistic) {
        jpaExportStatisticRepository.delete(exportStatistic);
    }

    /**
     * HS 코드 길이로 레벨 추정
     * 실제로는 ProductCategory 엔티티에서 정확한 레벨을 가져와야 함
     */
    private int estimateHsCodeLevel(String hsCode) {
        if (hsCode == null) return 1;
        int length = hsCode.length();
        return Math.min((length + 1) / 2, 6);
    }
}