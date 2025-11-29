package com.export.dashboard.domain.repository;

import com.export.dashboard.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ExportStatistic Aggregate의 도메인 리포지토리 인터페이스
 */
public interface ExportStatisticRepository {

    /**
     * 수출 통계 저장
     */
    ExportStatistic save(ExportStatistic exportStatistic);

    /**
     * ID로 수출 통계 조회
     */
    Optional<ExportStatistic> findById(Long id);

    /**
     * 특정 국가, 상품, 기간의 수출 통계 조회
     */
    Optional<ExportStatistic> findByCountryAndProductAndPeriod(Country country, ProductCategory productCategory, ExportPeriod period);

    /**
     * 특정 국가, 상품, 기간의 수출 통계 목록 조회 (복수 결과 허용)
     */
    List<ExportStatistic> findAllByCountryAndProductAndPeriod(Country country, ProductCategory productCategory, ExportPeriod period);

    /**
     * 특정 상품과 기간의 모든 수출 통계 조회
     */
    List<ExportStatistic> findByProductAndPeriod(ProductCategory productCategory, ExportPeriod period);

    /**
     * 특정 국가와 기간의 수출 통계를 금액 내림차순으로 조회
     */
    List<ExportStatistic> findByCountryAndPeriodOrderByValueDesc(Country country, ExportPeriod period);

    /**
     * 특정 국가, 상품의 여러 기간 수출 통계 조회
     */
    List<ExportStatistic> findByCountryAndProductInPeriods(Country country, ProductCategory productCategory, List<ExportPeriod> periods);

    /**
     * 특정 지역과 기간의 수출 통계 조회
     */
    List<ExportStatistic> findByRegionAndPeriod(String region, ExportPeriod period);

    /**
     * 년도별 수출 통계 조회
     */
    List<ExportStatistic> findByYear(Integer year);

    /**
     * 국가와 년도별 수출 통계 조회
     */
    List<ExportStatistic> findByCountryAndYear(Country country, Integer year);

    /**
     * 상품과 년도별 수출 통계 조회
     */
    List<ExportStatistic> findByProductCategoryAndYear(ProductCategory productCategory, Integer year);

    /**
     * 특정 기간 범위의 수출 통계 조회
     */
    List<ExportStatistic> findByPeriodBetween(ExportPeriod startPeriod, ExportPeriod endPeriod);

    /**
     * 페이징된 수출 통계 조회
     */
    Page<ExportStatistic> findAll(Pageable pageable);

    /**
     * 년도별 총 수출액 계산
     */
    Money getTotalExportValueByYear(Integer year);

    /**
     * 년도별 수출 국가 수 계산
     */
    Long countDistinctCountriesByYear(Integer year);

    /**
     * 년도별 수출 상품 수 계산
     */
    Long countDistinctProductsByYear(Integer year);

    /**
     * 상위 수출 국가 조회 (수출액 기준)
     */
    List<CountryExportSummary> getTopExportCountriesByYear(Integer year, int limit);

    /**
     * 상위 수출 상품 조회 (수출액 기준)
     */
    List<ProductExportSummary> getTopExportProductsByYear(Integer year, int limit);

    /**
     * 월별 수출 트렌드 조회
     */
    List<MonthlyExportTrend> getMonthlyExportTrend(Integer year);

    /**
     * 년도별 수출 트렌드 조회
     */
    List<YearlyExportTrend> getYearlyExportTrend(Integer startYear, Integer endYear);

    /**
     * 사용 가능한 모든 년도 조회
     */
    List<Integer> findAllYears();

    /**
     * 수출 통계 삭제
     */
    void delete(ExportStatistic exportStatistic);

    /**
     * 국가별 수출 요약 정보
     */
    record CountryExportSummary(
        CountryCode countryCode,
        String countryName,
        Money totalValue,
        Percentage marketShare
    ) {}

    /**
     * 상품별 수출 요약 정보
     */
    record ProductExportSummary(
        HsCode hsCode,
        String productName,
        Money totalValue,
        Percentage marketShare
    ) {}

    /**
     * 월별 수출 트렌드
     */
    record MonthlyExportTrend(
        ExportPeriod period,
        Money totalValue
    ) {}

    /**
     * 년도별 수출 트렌드
     */
    record YearlyExportTrend(
        Integer year,
        Money totalValue,
        Percentage growthRate
    ) {}
}