package com.export.dashboard.domain.service;

import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.ExportStatisticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ExportAnalysisService의 도메인 로직을 검증하는 TDD 테스트
 * Java 21의 Record Pattern과 Pattern Matching을 활용한 도메인 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("수출 분석 도메인 서비스 테스트")
class ExportAnalysisServiceTest {

    @Mock
    private ExportStatisticRepository exportStatisticRepository;

    private ExportAnalysisService exportAnalysisService;

    // Test fixture - Java 21 record pattern을 활용한 테스트 데이터
    private Country korea;
    private Country china;
    private ProductCategory semiconductors;
    private ProductCategory electronics;

    @BeforeEach
    void setUp() {
        exportAnalysisService = new ExportAnalysisService(exportStatisticRepository);

        // 테스트 데이터 생성 - Java 21 pattern matching 활용
        korea = Country.create("KOR", "대한민국", "Republic of Korea", "East Asia", "Asia");
        china = Country.create("CHN", "중국", "China", "East Asia", "Asia");

        semiconductors = ProductCategory.create("8542", 2, "반도체", "Semiconductors");
        electronics = ProductCategory.create("85", 1, "전자제품", "Electronics");
    }

    @Nested
    @DisplayName("성장률 분석 기능")
    class GrowthRateAnalysisTest {

        @Test
        @DisplayName("전년 동월 대비 성장률을 정확히 계산해야 한다")
        void shouldCalculateGrowthRateYoyRateCorrectly() {
            // Given - 현재와 이전 년도 수출 통계
            var currentPeriod = ExportPeriod.of(2024, 3);
            var previousPeriod = ExportPeriod.of(2023, 3);

            var currentValue = Money.usd(new BigDecimal("1000000"));
            var previousValue = Money.usd(new BigDecimal("800000"));

            var currentStat = ExportStatistic.create(korea, semiconductors, currentPeriod, currentValue);
            var previousStat = ExportStatistic.create(korea, semiconductors, previousPeriod, previousValue);

            when(exportStatisticRepository.findAllByCountryAndProductAndPeriod(
                korea, semiconductors, previousPeriod))
                .thenReturn(List.of(previousStat));

            // When - 성장률 계산
            var growthRate = exportAnalysisService.calculateGrowthRate(currentStat);

            // Then - 25% 성장률 확인
            assertThat(growthRate).isNotNull();
            assertThat(growthRate.value()).isEqualByComparingTo(new BigDecimal("25.00"));
        }

        @Test
        @DisplayName("이전 년도 데이터가 없으면 성장률 계산이 불가능해야 한다")
        void shouldReturnNullWhenPreviousYearDataNotAvailable() {
            // Given - 현재 년도만 데이터가 있는 경우
            var currentPeriod = ExportPeriod.of(2024, 3);
            var currentValue = Money.usd(new BigDecimal("1000000"));
            var currentStat = ExportStatistic.create(korea, semiconductors, currentPeriod, currentValue);

            when(exportStatisticRepository.findAllByCountryAndProductAndPeriod(
                any(Country.class), any(ProductCategory.class), any(ExportPeriod.class)))
                .thenReturn(List.of());

            // When - 성장률 계산 시도
            var growthRate = exportAnalysisService.calculateGrowthRate(currentStat);

            // Then - null 반환
            assertThat(growthRate).isNull();
        }
    }

    @Nested
    @DisplayName("시장 점유율 분석 기능")
    class MarketShareAnalysisTest {

        @Test
        @DisplayName("특정 상품의 국가별 시장 점유율을 정확히 계산해야 한다")
        void shouldCalculateMarketShareCorrectly() {
            // Given - 같은 상품에 대한 여러 국가의 수출 데이터
            var period = ExportPeriod.of(2024, 3);

            var koreaExport = ExportStatistic.create(korea, semiconductors, period,
                Money.usd(new BigDecimal("500000")));
            var chinaExport = ExportStatistic.create(china, semiconductors, period,
                Money.usd(new BigDecimal("300000")));

            var allExports = List.of(koreaExport, chinaExport);

            when(exportStatisticRepository.findByProductAndPeriod(semiconductors, period))
                .thenReturn(allExports);

            // When - 시장 점유율 계산
            var marketShare = exportAnalysisService.calculateMarketShare(koreaExport);

            // Then - 62.5% 점유율 확인 (500,000 / 800,000)
            assertThat(marketShare).isNotNull();
            assertThat(marketShare.value()).isEqualByComparingTo(new BigDecimal("62.50"));
        }

        @Test
        @DisplayName("총 시장 규모가 0이면 시장 점유율이 0이어야 한다")
        void shouldReturnZeroMarketShareWhenTotalMarketIsZero() {
            // Given - 시장 데이터가 없는 경우
            var period = ExportPeriod.of(2024, 3);
            var koreaExport = ExportStatistic.create(korea, semiconductors, period,
                Money.usd(BigDecimal.ZERO));

            when(exportStatisticRepository.findByProductAndPeriod(semiconductors, period))
                .thenReturn(List.of());

            // When - 시장 점유율 계산
            var marketShare = exportAnalysisService.calculateMarketShare(koreaExport);

            // Then - 0% 점유율
            assertThat(marketShare).isNotNull();
            assertThat(marketShare.value()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("상위 수출 상품 분석 기능")
    class TopExportAnalysisTest {

        @Test
        @DisplayName("특정 국가의 상위 N개 수출 상품을 반환해야 한다")
        void shouldReturnTopNExportProducts() {
            // Given - 다양한 상품의 수출 데이터
            var period = ExportPeriod.of(2024, 3);

            var semiconductorExport = ExportStatistic.create(korea, semiconductors, period,
                Money.usd(new BigDecimal("1000000")));
            var electronicsExport = ExportStatistic.create(korea, electronics, period,
                Money.usd(new BigDecimal("800000")));

            var exports = List.of(semiconductorExport, electronicsExport);

            when(exportStatisticRepository.findByCountryAndPeriodOrderByValueDesc(korea, period))
                .thenReturn(exports);

            // When - 상위 5개 상품 조회
            var topProducts = exportAnalysisService.getTopExportProducts(korea, period, 5);

            // Then - 수출액 순으로 정렬된 상품 목록
            assertThat(topProducts)
                .hasSize(2)
                .extracting(ExportStatistic::getExportValue)
                .extracting(Money::amount)
                .containsExactly(
                    new BigDecimal("1000000.00"),
                    new BigDecimal("800000.00")
                );
        }

        @Test
        @DisplayName("요청한 개수보다 적은 상품만 있어도 정상 처리되어야 한다")
        void shouldHandleFewerProductsThanRequested() {
            // Given - 요청보다 적은 수의 상품
            var period = ExportPeriod.of(2024, 3);
            var exports = List.of(
                ExportStatistic.create(korea, semiconductors, period,
                    Money.usd(new BigDecimal("1000000")))
            );

            when(exportStatisticRepository.findByCountryAndPeriodOrderByValueDesc(korea, period))
                .thenReturn(exports);

            // When - 상위 5개 요청하지만 1개만 존재
            var topProducts = exportAnalysisService.getTopExportProducts(korea, period, 5);

            // Then - 1개만 반환
            assertThat(topProducts).hasSize(1);
        }
    }

    @Nested
    @DisplayName("수출 트렌드 분석 기능")
    class ExportTrendAnalysisTest {

        @Test
        @DisplayName("최근 N개월간의 수출 트렌드를 분석해야 한다")
        void shouldAnalyzeExportTrendForRecentMonths() {
            // Given - 최근 3개월 수출 데이터 (현재 날짜 기준)
            var currentDate = java.time.LocalDate.now();
            var periods = java.util.stream.IntStream.range(0, 3)
                .mapToObj(i -> currentDate.minusMonths(i))
                .map(date -> ExportPeriod.of(date.getYear(), date.getMonthValue()))
                .toList();

            var exports = periods.stream()
                .map(period -> ExportStatistic.create(korea, semiconductors, period,
                    Money.usd(new BigDecimal("1000000"))))
                .toList();

            when(exportStatisticRepository.findByCountryAndProductInPeriods(
                korea, semiconductors, periods))
                .thenReturn(exports);

            // When - 트렌드 분석
            var trend = exportAnalysisService.analyzeExportTrend(korea, semiconductors, 3);

            // Then - 안정적인 트렌드
            assertThat(trend).isNotNull();
            assertThat(trend.isStable()).isTrue();
            assertThat(trend.monthlyData()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 검증")
    class BusinessRuleValidationTest {

        @Test
        @DisplayName("null 파라미터로 서비스 호출 시 예외가 발생해야 한다")
        void shouldThrowExceptionForNullParameters() {
            // When & Then
            assertThatThrownBy(() -> exportAnalysisService.calculateGrowthRate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수출 통계는 필수입니다");

            assertThatThrownBy(() -> exportAnalysisService.calculateMarketShare(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수출 통계는 필수입니다");
        }

        @Test
        @DisplayName("비활성화된 국가나 상품에 대한 분석 요청 시 예외가 발생해야 한다")
        void shouldThrowExceptionForInactiveEntities() {
            // Given - 비활성화된 국가
            var inactiveCountry = Country.create("USA", "미국", "United States");
            inactiveCountry.deactivate();

            var period = ExportPeriod.of(2024, 3);

            // When & Then
            assertThatThrownBy(() ->
                exportAnalysisService.getTopExportProducts(inactiveCountry, period, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비활성화된 국가입니다");
        }
    }
}