package com.export.dashboard.batch.processor;

import com.export.dashboard.batch.model.CustomsApiResponse;
import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.CountryRepository;
import com.export.dashboard.domain.repository.ProductCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 관세청 API 데이터를 ExportStatistic 엔티티로 변환하는 Processor
 */
@Component
public class CustomsDataProcessor implements ItemProcessor<CustomsApiResponse.TradeData, ExportStatistic> {

    private static final Logger logger = LoggerFactory.getLogger(CustomsDataProcessor.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String WORLD_COUNTRY_CODE = "WLD";

    private final CountryRepository countryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private Country worldCountry; // 캐시: WORLD Country

    public CustomsDataProcessor(
        CountryRepository countryRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        this.countryRepository = countryRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Override
    public ExportStatistic process(CustomsApiResponse.TradeData item) throws Exception {
        try {
            // 1. WORLD Country 조회 (캐시 사용)
            if (worldCountry == null) {
                worldCountry = countryRepository.findByCountryCode(CountryCode.from(WORLD_COUNTRY_CODE))
                    .orElseThrow(() -> new IllegalStateException(
                        "WORLD Country(WLD)를 찾을 수 없습니다. DB 마이그레이션을 확인하세요."));
            }

            // 2. ProductCategory 조회 또는 생성
            ProductCategory productCategory = getOrCreateProductCategory(item);

            // 3. 기간 파싱
            ExportPeriod period = parsePeriod(item.getYear());

            // 4. 금액 및 중량 파싱
            BigDecimal exportValue = item.getExpDlrAsBigDecimal();
            BigDecimal exportWeight = item.getExpWgtAsBigDecimal();
            BigDecimal importValue = item.getImpDlrAsBigDecimal();
            BigDecimal importWeight = item.getImpWgtAsBigDecimal();
            BigDecimal balanceOfPayments = item.getBalPaymentsAsBigDecimal();

            // 5. ExportStatistic 생성
            ExportStatistic statistic = ExportStatistic.create(
                worldCountry,
                productCategory,
                period.year(),
                period.month(),
                exportValue
            );

            // 6. 수출 중량 업데이트
            if (exportWeight != null && exportWeight.compareTo(BigDecimal.ZERO) > 0) {
                statistic.updateWeight(exportWeight);
            }

            // 7. 수입 데이터 업데이트
            if (importValue != null && importValue.compareTo(BigDecimal.ZERO) > 0) {
                statistic.updateImportData(
                    Money.usd(importValue),
                    importWeight,
                    balanceOfPayments
                );
            }

            // 8. 데이터 출처 표시
            statistic.markAsCustomsApiData();

            logger.debug("관세청 데이터 변환 완료: hsCode={}, period={}, exportValue={}",
                item.getHsCode(), period, exportValue);

            return statistic;

        } catch (Exception e) {
            logger.error("관세청 데이터 처리 실패: hsCode={}, error={}",
                item.getHsCode(), e.getMessage(), e);
            // 처리 실패한 항목은 건너뛰기
            return null;
        }
    }

    /**
     * ProductCategory 조회 또는 생성
     */
    private ProductCategory getOrCreateProductCategory(CustomsApiResponse.TradeData item) {
        String hsCode = item.getHsCode();
        String productName = item.getStatKor();

        return productCategoryRepository.findByHsCode(HsCode.from(hsCode))
            .orElseGet(() -> {
                logger.info("새로운 ProductCategory 생성: hsCode={}, name={}", hsCode, productName);

                ProductCategory newCategory = ProductCategory.create(
                    hsCode,
                    determineHsLevel(hsCode),
                    productName != null ? productName : "Unknown",
                    productName != null ? productName : "Unknown"
                );

                return productCategoryRepository.save(newCategory);
            });
    }

    /**
     * HS코드 레벨 결정 (자릿수 기준)
     */
    private int determineHsLevel(String hsCode) {
        if (hsCode == null) {
            return 2;
        }
        int length = hsCode.trim().length();
        if (length <= 2) return 2;
        if (length <= 4) return 4;
        if (length <= 6) return 6;
        if (length <= 8) return 8;
        return 10;
    }

    /**
     * 기간 문자열을 ExportPeriod로 파싱
     *
     * @param yearMonth YYYYMM 형식
     * @return ExportPeriod
     */
    private ExportPeriod parsePeriod(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
            return ExportPeriod.of(ym.getYear(), ym.getMonthValue());
        } catch (Exception e) {
            logger.error("기간 파싱 실패: {}", yearMonth, e);
            throw new IllegalArgumentException("잘못된 기간 형식: " + yearMonth);
        }
    }
}
