package com.export.dashboard.domain.event;

import com.export.dashboard.domain.model.Money;
import com.export.dashboard.domain.model.ExportPeriod;

/**
 * 임계값 초과 관련 이벤트 데이터
 */
public record ThresholdData(
    String countryCode,
    String hsCode,
    ExportPeriod period,
    Money currentValue,
    Money thresholdValue,
    String thresholdType,
    String alertLevel
) {

    public ThresholdData {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
        if (hsCode == null || hsCode.trim().isEmpty()) {
            throw new IllegalArgumentException("HS 코드는 필수입니다");
        }
        if (period == null) {
            throw new IllegalArgumentException("기간은 필수입니다");
        }
        if (currentValue == null) {
            throw new IllegalArgumentException("현재 값은 필수입니다");
        }
        if (thresholdValue == null) {
            throw new IllegalArgumentException("임계값은 필수입니다");
        }
        if (thresholdType == null || thresholdType.trim().isEmpty()) {
            throw new IllegalArgumentException("임계값 타입은 필수입니다");
        }
        if (alertLevel == null || alertLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("경고 수준은 필수입니다");
        }
    }

    /**
     * 임계값 초과 비율 계산
     */
    public double getExceedanceRatio() {
        if (thresholdValue.isZero()) {
            return 0.0;
        }

        return currentValue.amount()
                          .divide(thresholdValue.amount(), 4, java.math.RoundingMode.HALF_UP)
                          .doubleValue();
    }

    /**
     * 경고 수준이 위험한지 확인
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(alertLevel) || "HIGH".equalsIgnoreCase(alertLevel);
    }
}