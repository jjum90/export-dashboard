package com.export.dashboard.domain.event;

import com.export.dashboard.domain.model.Money;
import com.export.dashboard.domain.model.ExportPeriod;

import java.math.BigDecimal;

/**
 * 수출 통계 이벤트 데이터
 * Java 21 Record를 활용한 불변 이벤트 페이로드
 */
public record ExportStatisticData(
    String countryCode,
    String countryName,
    String hsCode,
    String productName,
    ExportPeriod period,
    Money exportValue,
    BigDecimal exportWeight,
    BigDecimal previousValue
) {

    public ExportStatisticData {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
        if (hsCode == null || hsCode.trim().isEmpty()) {
            throw new IllegalArgumentException("HS 코드는 필수입니다");
        }
        if (period == null) {
            throw new IllegalArgumentException("기간은 필수입니다");
        }
        if (exportValue == null) {
            throw new IllegalArgumentException("수출 금액은 필수입니다");
        }
    }

    /**
     * 성장률 계산 (이전 값이 있는 경우)
     */
    public BigDecimal calculateGrowthRate() {
        if (previousValue == null || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        var current = exportValue.amount();
        var growth = current.subtract(previousValue)
                          .divide(previousValue, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));

        return growth;
    }

    /**
     * 중량 당 가격 계산
     */
    public Money calculateValuePerKg() {
        if (exportWeight == null || exportWeight.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("중량 정보가 없어 단위 가격을 계산할 수 없습니다");
        }

        return exportValue.divide(exportWeight);
    }
}