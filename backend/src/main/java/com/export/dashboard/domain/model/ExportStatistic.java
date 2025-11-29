package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import com.export.dashboard.domain.event.ExportStatisticCreated;
import com.export.dashboard.domain.event.ExportStatisticUpdated;
import com.export.dashboard.domain.event.ExportStatisticData;
import com.export.dashboard.domain.event.ExportThresholdExceeded;
import com.export.dashboard.domain.event.ThresholdData;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

/**
 * 수출 통계 Aggregate Root
 * 특정 국가와 상품의 수출 데이터를 관리하고 비즈니스 로직을 캡슐화
 */
@Entity
@Table(name = "export_statistics")
public class ExportStatistic extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategory productCategory;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "year", column = @Column(name = "year", nullable = false)),
        @AttributeOverride(name = "month", column = @Column(name = "month", nullable = false))
    })
    private ExportPeriod period;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "export_value_usd", nullable = false, precision = 15, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money exportValue;

    @Column(name = "export_weight_kg", precision = 15, scale = 3)
    private BigDecimal exportWeightKg;

    @Column(name = "export_quantity", precision = 15, scale = 3)
    private BigDecimal exportQuantity;

    @Column(name = "quantity_unit", length = 20)
    private String quantityUnit;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "growth_rate_yoy", precision = 5, scale = 2))
    private Percentage growthRateYoy;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "market_share", precision = 5, scale = 2))
    private Percentage marketShare;

    @Version
    private Long version;

    protected ExportStatistic() {
        // JPA 전용
    }

    public ExportStatistic(Country country, ProductCategory productCategory, ExportPeriod period, Money exportValue) {
        this.country = validateCountry(country);
        this.productCategory = validateProductCategory(productCategory);
        this.period = validatePeriod(period);
        this.exportValue = validateExportValue(exportValue);
        this.growthRateYoy = Percentage.zero();
        this.marketShare = Percentage.zero();

        // 수출 통계 생성 이벤트 발행
        publishCreatedEvent();
    }

    // Factory methods
    public static ExportStatistic create(Country country, ProductCategory productCategory,
                                       ExportPeriod period, Money exportValue) {
        return new ExportStatistic(country, productCategory, period, exportValue);
    }

    public static ExportStatistic create(Country country, ProductCategory productCategory,
                                       Integer year, Integer month, BigDecimal exportValueUsd) {
        return new ExportStatistic(
            country,
            productCategory,
            ExportPeriod.of(year, month),
            Money.usd(exportValueUsd)
        );
    }

    // Business methods
    public void updateExportValue(Money newValue) {
        Money previousValue = this.exportValue;
        this.exportValue = validateExportValue(newValue);

        // 수출 통계 업데이트 이벤트 발행
        publishUpdatedEvent(previousValue);

        // 임계값 검사
        checkAndPublishThresholdEvent(newValue);
    }

    public void updateWeight(BigDecimal weightKg) {
        if (weightKg != null && weightKg.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidValueObjectException("수출 중량은 0 이상이어야 합니다.");
        }
        this.exportWeightKg = weightKg;
    }

    public void updateQuantity(BigDecimal quantity, String unit) {
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidValueObjectException("수출 수량은 0 이상이어야 합니다.");
        }
        if (quantity != null && (unit == null || unit.trim().isEmpty())) {
            throw new InvalidValueObjectException("수출 수량이 있으면 단위도 필수입니다.");
        }
        this.exportQuantity = quantity;
        this.quantityUnit = unit != null ? unit.trim() : null;
    }

    public void calculateGrowthRate(Money previousYearValue) {
        if (previousYearValue == null || previousYearValue.isZero()) {
            this.growthRateYoy = Percentage.zero();
            return;
        }

        Money difference = exportValue.subtract(previousYearValue);
        this.growthRateYoy = Percentage.calculate(difference.amount(), previousYearValue.amount());
    }

    public void updateMarketShare(Money totalMarketValue) {
        if (totalMarketValue == null || totalMarketValue.isZero()) {
            this.marketShare = Percentage.zero();
            return;
        }

        this.marketShare = Percentage.calculate(exportValue.amount(), totalMarketValue.amount());
    }

    public Money calculateValuePerKg() {
        if (exportWeightKg == null || exportWeightKg.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidValueObjectException("중량 정보가 없어 단위 가격을 계산할 수 없습니다.");
        }
        return exportValue.divide(exportWeightKg);
    }

    public boolean hasQuantityInfo() {
        return exportQuantity != null && quantityUnit != null && !quantityUnit.trim().isEmpty();
    }

    public boolean hasWeightInfo() {
        return exportWeightKg != null && exportWeightKg.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isFromPreviousYear() {
        ExportPeriod currentPeriod = ExportPeriod.current();
        return period.year() < currentPeriod.year();
    }

    public boolean isSamePeriod(ExportPeriod otherPeriod) {
        return period.equals(otherPeriod);
    }

    public ExportStatistic getPreviousYearSameMonth() {
        ExportPeriod previousYearPeriod = period.sameMonthPreviousYear();
        // 이는 Repository에서 조회해야 하는 로직이므로 여기서는 구조만 제공
        throw new UnsupportedOperationException("Repository를 통해 조회해야 합니다.");
    }

    // Validation methods
    private Country validateCountry(Country country) {
        if (country == null) {
            throw new InvalidValueObjectException("국가 정보는 필수입니다.");
        }
        if (!country.isActive()) {
            throw new InvalidValueObjectException("비활성화된 국가입니다: " + country.getCountryCode());
        }
        return country;
    }

    private ProductCategory validateProductCategory(ProductCategory productCategory) {
        if (productCategory == null) {
            throw new InvalidValueObjectException("상품 카테고리 정보는 필수입니다.");
        }
        if (!productCategory.isActive()) {
            throw new InvalidValueObjectException("비활성화된 상품 카테고리입니다: " + productCategory.getHsCode());
        }
        return productCategory;
    }

    private ExportPeriod validatePeriod(ExportPeriod period) {
        if (period == null) {
            throw new InvalidValueObjectException("수출 기간은 필수입니다.");
        }
        ExportPeriod currentPeriod = ExportPeriod.current();
        if (period.isAfter(currentPeriod)) {
            throw new InvalidValueObjectException("미래 날짜의 수출 통계는 생성할 수 없습니다.");
        }
        return period;
    }

    private Money validateExportValue(Money exportValue) {
        if (exportValue == null) {
            throw new InvalidValueObjectException("수출 금액은 필수입니다.");
        }
        if (!exportValue.isPositive() && !exportValue.isZero()) {
            throw new InvalidValueObjectException("수출 금액은 0 이상이어야 합니다.");
        }
        return exportValue;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Country getCountry() {
        return country;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public ExportPeriod getPeriod() {
        return period;
    }

    public Money getExportValue() {
        return exportValue;
    }

    public BigDecimal getExportWeightKg() {
        return exportWeightKg;
    }

    public BigDecimal getExportQuantity() {
        return exportQuantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public Percentage getGrowthRateYoy() {
        return growthRateYoy;
    }

    public Percentage getMarketShare() {
        return marketShare;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public String getAggregateId() {
        return String.format("%s-%s-%s",
            country != null ? country.getCountryCode().value() : "unknown",
            productCategory != null ? productCategory.getHsCode().value() : "unknown",
            period != null ? period.toString() : "unknown"
        );
    }

    // Event publishing helper methods
    private void publishCreatedEvent() {
        var eventData = createEventData(null);
        var event = new ExportStatisticCreated(
            UUID.randomUUID(),
            Instant.now(),
            getAggregateId(),
            eventData
        );
        registerEvent(event);
    }

    private void publishUpdatedEvent(Money previousValue) {
        var eventData = createEventData(previousValue != null ? previousValue.amount() : null);
        var event = new ExportStatisticUpdated(
            UUID.randomUUID(),
            Instant.now(),
            getAggregateId(),
            eventData
        );
        registerEvent(event);
    }

    private void checkAndPublishThresholdEvent(Money currentValue) {
        // 예시: 월간 수출액이 100만 달러를 초과하는 경우 임계값 초과 이벤트 발행
        var threshold = Money.usd(new BigDecimal("1000000"));

        if (currentValue.amount().compareTo(threshold.amount()) > 0) {
            var thresholdData = new ThresholdData(
                country.getCountryCode().value(),
                productCategory.getHsCode().value(),
                period,
                currentValue,
                threshold,
                "MONTHLY_EXPORT_VALUE",
                "HIGH"
            );

            var event = new ExportThresholdExceeded(
                UUID.randomUUID(),
                Instant.now(),
                getAggregateId(),
                thresholdData
            );

            registerEvent(event);
        }
    }

    private ExportStatisticData createEventData(BigDecimal previousValue) {
        return new ExportStatisticData(
            country.getCountryCode().value(),
            country.getNameKo(),
            productCategory.getHsCode().value(),
            productCategory.getNameKo(),
            period,
            exportValue,
            exportWeightKg,
            previousValue
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExportStatistic that = (ExportStatistic) obj;
        return Objects.equals(country, that.country) &&
               Objects.equals(productCategory, that.productCategory) &&
               Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, productCategory, period);
    }

    @Override
    public String toString() {
        return String.format("ExportStatistic[country=%s, product=%s, period=%s, value=%s]",
            country.getCountryCode(), productCategory.getHsCode(), period, exportValue);
    }
}