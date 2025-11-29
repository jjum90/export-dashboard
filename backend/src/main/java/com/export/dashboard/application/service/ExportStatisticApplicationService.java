package com.export.dashboard.application.service;

import com.export.dashboard.application.dto.*;
import com.export.dashboard.domain.exception.CountryNotFoundException;
import com.export.dashboard.domain.exception.ExportStatisticNotFoundException;
import com.export.dashboard.domain.exception.ProductCategoryNotFoundException;
import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.CountryRepository;
import com.export.dashboard.domain.repository.ExportStatisticRepository;
import com.export.dashboard.domain.repository.ProductCategoryRepository;
import com.export.dashboard.domain.service.ExportAnalyticsDomainService;
import com.export.dashboard.domain.service.ExportStatisticDomainService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

/**
 * 수출 통계 관리 애플리케이션 서비스
 * 수출 통계 관련 유스케이스를 조율하고 트랜잭션 경계를 관리
 */
@Service
@Transactional(readOnly = true)
public class ExportStatisticApplicationService {

    private final ExportStatisticRepository exportStatisticRepository;
    private final CountryRepository countryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ExportStatisticDomainService exportStatisticDomainService;
    private final ExportAnalyticsDomainService exportAnalyticsDomainService;

    public ExportStatisticApplicationService(
            ExportStatisticRepository exportStatisticRepository,
            CountryRepository countryRepository,
            ProductCategoryRepository productCategoryRepository,
            ExportStatisticDomainService exportStatisticDomainService,
            ExportAnalyticsDomainService exportAnalyticsDomainService) {
        this.exportStatisticRepository = exportStatisticRepository;
        this.countryRepository = countryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.exportStatisticDomainService = exportStatisticDomainService;
        this.exportAnalyticsDomainService = exportAnalyticsDomainService;
    }

    /**
     * 새로운 수출 통계 생성
     */
    @Transactional
    public ExportStatisticResponse createExportStatistic(CreateExportStatisticRequest request) {
        // 참조 데이터 조회 및 검증
        Country country = countryRepository.findById(request.countryId())
            .orElseThrow(() -> CountryNotFoundException.withId(request.countryId()));

        ProductCategory productCategory = productCategoryRepository.findById(request.productCategoryId())
            .orElseThrow(() -> ProductCategoryNotFoundException.withId(request.productCategoryId()));

        // 도메인 객체 생성
        ExportStatistic statistic = ExportStatistic.create(
            country,
            productCategory,
            ExportPeriod.of(request.year(), request.month()),
            Money.usd(request.exportValueUsd())
        );

        // 추가 정보 설정
        if (request.exportWeightKg() != null) {
            statistic.updateWeight(request.exportWeightKg());
        }

        if (request.exportQuantity() != null) {
            statistic.updateQuantity(request.exportQuantity(), request.quantityUnit());
        }

        // 도메인 서비스를 통한 비즈니스 로직 수행
        exportStatisticDomainService.validateExportDataConsistency(statistic);
        exportStatisticDomainService.calculateAndUpdateGrowthRate(statistic);
        exportStatisticDomainService.calculateAndUpdateMarketShare(statistic);

        // 저장
        ExportStatistic savedStatistic = exportStatisticRepository.save(statistic);

        return ExportStatisticResponse.from(savedStatistic);
    }

    /**
     * 수출 통계 ID로 조회
     */
    public ExportStatisticResponse getExportStatisticById(Long id) {
        ExportStatistic statistic = exportStatisticRepository.findById(id)
            .orElseThrow(() -> ExportStatisticNotFoundException.withId(id));

        return ExportStatisticResponse.from(statistic);
    }

    /**
     * 년도별 수출 통계 조회
     */
    @Cacheable(value = "export-statistics", key = "#year")
    public List<ExportStatisticResponse> getExportStatisticsByYear(Integer year) {
        return exportStatisticRepository.findByYear(year)
            .stream()
            .map(ExportStatisticResponse::from)
            .toList();
    }

    /**
     * 국가와 년도별 수출 통계 조회
     */
    @Cacheable(value = "export-statistics", key = "#countryId + '_' + #year")
    public List<ExportStatisticResponse> getExportStatisticsByCountryAndYear(Long countryId, Integer year) {
        Country country = countryRepository.findById(countryId)
            .orElseThrow(() -> CountryNotFoundException.withId(countryId));

        return exportStatisticRepository.findByCountryAndYear(country, year)
            .stream()
            .map(ExportStatisticResponse::from)
            .toList();
    }

    /**
     * 상품과 년도별 수출 통계 조회
     */
    @Cacheable(value = "export-statistics", key = "#productCategoryId + '_' + #year")
    public List<ExportStatisticResponse> getExportStatisticsByProductAndYear(Long productCategoryId, Integer year) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId)
            .orElseThrow(() -> ProductCategoryNotFoundException.withId(productCategoryId));

        return exportStatisticRepository.findByProductCategoryAndYear(productCategory, year)
            .stream()
            .map(ExportStatisticResponse::from)
            .toList();
    }

    /**
     * 페이징된 수출 통계 조회
     */
    public Page<ExportStatisticResponse> getExportStatistics(Pageable pageable) {
        return exportStatisticRepository.findAll(pageable)
            .map(ExportStatisticResponse::from);
    }

    /**
     * 대시보드 요약 정보 생성
     */
    @Cacheable(value = "dashboard-summary", key = "#year")
    public DashboardSummaryResponse getDashboardSummary(Integer year) {
        ExportAnalyticsDomainService.DashboardSummary summary =
            exportAnalyticsDomainService.generateDashboardSummary(year);

        return DashboardSummaryResponse.from(summary);
    }

    /**
     * 수출 다양성 지수 조회
     */
    @Cacheable(value = "export-analytics", key = "'diversity_' + #year")
    public BigDecimal getExportDiversityIndex(Integer year) {
        return exportAnalyticsDomainService.calculateExportDiversityIndex(year);
    }

    /**
     * 지역별 수출 집중도 분석
     */
    @Cacheable(value = "export-analytics", key = "'concentration_' + #year")
    public RegionalConcentrationResponse getRegionalConcentration(Integer year) {
        ExportAnalyticsDomainService.RegionalConcentration concentration =
            exportAnalyticsDomainService.analyzeRegionalConcentration(year);

        return RegionalConcentrationResponse.from(concentration);
    }

    /**
     * 계절성 분석
     */
    @Cacheable(value = "export-analytics", key = "'seasonality_' + #year")
    public SeasonalityAnalysisResponse getSeasonalityAnalysis(Integer year) {
        ExportAnalyticsDomainService.SeasonalityAnalysis analysis =
            exportAnalyticsDomainService.analyzeSeasonality(year);

        return SeasonalityAnalysisResponse.from(analysis);
    }

    /**
     * 성장 추세 분석
     */
    @Cacheable(value = "export-analytics", key = "'growth_' + #startYear + '_' + #endYear")
    public GrowthTrendAnalysisResponse getGrowthTrendAnalysis(Integer startYear, Integer endYear) {
        ExportAnalyticsDomainService.GrowthTrendAnalysis analysis =
            exportAnalyticsDomainService.analyzeGrowthTrend(startYear, endYear);

        return GrowthTrendAnalysisResponse.from(analysis);
    }

    /**
     * 사용 가능한 년도 목록 조회
     */
    @Cacheable(value = "available-years")
    public List<Integer> getAvailableYears() {
        return exportStatisticRepository.findAllYears();
    }

    /**
     * 수출 통계 업데이트
     */
    @Transactional
    public ExportStatisticResponse updateExportStatistic(Long id, CreateExportStatisticRequest request) {
        ExportStatistic statistic = exportStatisticRepository.findById(id)
            .orElseThrow(() -> ExportStatisticNotFoundException.withId(id));

        // 수출 금액 업데이트
        statistic.updateExportValue(Money.usd(request.exportValueUsd()));

        // 중량 정보 업데이트
        if (request.exportWeightKg() != null) {
            statistic.updateWeight(request.exportWeightKg());
        }

        // 수량 정보 업데이트
        if (request.exportQuantity() != null) {
            statistic.updateQuantity(request.exportQuantity(), request.quantityUnit());
        }

        // 성장률 및 시장점유율 재계산
        exportStatisticDomainService.calculateAndUpdateGrowthRate(statistic);
        exportStatisticDomainService.calculateAndUpdateMarketShare(statistic);

        ExportStatistic savedStatistic = exportStatisticRepository.save(statistic);

        return ExportStatisticResponse.from(savedStatistic);
    }

    /**
     * 수출 통계 삭제
     */
    @Transactional
    public void deleteExportStatistic(Long id) {
        ExportStatistic statistic = exportStatisticRepository.findById(id)
            .orElseThrow(() -> ExportStatisticNotFoundException.withId(id));

        exportStatisticRepository.delete(statistic);
    }

    /**
     * 국가별 수출 성과 분석
     */
    @Cacheable(value = "export-analytics", key = "'country_performance_' + #countryId + '_' + #year")
    public CountryPerformanceResponse getCountryPerformance(Long countryId, Integer year) {
        Country country = countryRepository.findById(countryId)
            .orElseThrow(() -> CountryNotFoundException.withId(countryId));

        ExportStatisticDomainService.CountryExportPerformance performance =
            exportStatisticDomainService.analyzeCountryPerformance(country, year);

        return CountryPerformanceResponse.from(performance);
    }

    /**
     * 상품별 수출 성과 분석
     */
    @Cacheable(value = "export-analytics", key = "'product_performance_' + #productCategoryId + '_' + #year")
    public ProductPerformanceResponse getProductPerformance(Long productCategoryId, Integer year) {
        ProductCategory productCategory = productCategoryRepository.findById(productCategoryId)
            .orElseThrow(() -> ProductCategoryNotFoundException.withId(productCategoryId));

        ExportStatisticDomainService.ProductExportPerformance performance =
            exportStatisticDomainService.analyzeProductPerformance(productCategory, year);

        return ProductPerformanceResponse.from(performance);
    }

    // Response Record classes
    public record RegionalConcentrationResponse(
        Integer year,
        BigDecimal totalValue,
        BigDecimal top5Concentration,
        BigDecimal top10Concentration,
        Integer totalCountries
    ) {
        public static RegionalConcentrationResponse from(ExportAnalyticsDomainService.RegionalConcentration concentration) {
            return new RegionalConcentrationResponse(
                concentration.year(),
                concentration.totalValue().amount(),
                concentration.top5Concentration().value(),
                concentration.top10Concentration().value(),
                concentration.totalCountries()
            );
        }
    }

    public record SeasonalityAnalysisResponse(
        Integer year,
        BigDecimal coefficientOfVariation,
        Integer peakMonth,
        Integer troughMonth,
        Boolean hasSeasonality
    ) {
        public static SeasonalityAnalysisResponse from(ExportAnalyticsDomainService.SeasonalityAnalysis analysis) {
            return new SeasonalityAnalysisResponse(
                analysis.year(),
                analysis.coefficientOfVariation(),
                analysis.peakMonth(),
                analysis.troughMonth(),
                analysis.hasSeasonality()
            );
        }
    }

    public record GrowthTrendAnalysisResponse(
        Integer startYear,
        Integer endYear,
        BigDecimal compoundAnnualGrowthRate,
        String trendType
    ) {
        public static GrowthTrendAnalysisResponse from(ExportAnalyticsDomainService.GrowthTrendAnalysis analysis) {
            return new GrowthTrendAnalysisResponse(
                analysis.startYear(),
                analysis.endYear(),
                analysis.compoundAnnualGrowthRate(),
                analysis.trendType()
            );
        }
    }

    public record CountryPerformanceResponse(
        String countryCode,
        Integer year,
        BigDecimal totalExportValue,
        BigDecimal growthRate,
        Integer exportedProductCount
    ) {
        public static CountryPerformanceResponse from(ExportStatisticDomainService.CountryExportPerformance performance) {
            return new CountryPerformanceResponse(
                performance.countryCode().value(),
                performance.year(),
                performance.totalExportValue().amount(),
                performance.growthRate().value(),
                performance.exportedProductCount()
            );
        }
    }

    public record ProductPerformanceResponse(
        String hsCode,
        Integer year,
        BigDecimal totalExportValue,
        BigDecimal growthRate,
        Integer exportingCountryCount
    ) {
        public static ProductPerformanceResponse from(ExportStatisticDomainService.ProductExportPerformance performance) {
            return new ProductPerformanceResponse(
                performance.hsCode().value(),
                performance.year(),
                performance.totalExportValue().amount(),
                performance.growthRate().value(),
                performance.exportingCountryCount()
            );
        }
    }
}