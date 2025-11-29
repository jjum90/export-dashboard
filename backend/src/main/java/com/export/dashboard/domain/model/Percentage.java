package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 백분율을 나타내는 Value Object
 * 성장률, 시장점유율 등의 백분율 값을 안전하게 처리
 */
public record Percentage(BigDecimal value) {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public Percentage {
        if (value == null) {
            throw new InvalidValueObjectException("백분율 값은 필수입니다.");
        }

        // 스케일 정규화
        value = value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }

    public static Percentage zero() {
        return new Percentage(BigDecimal.ZERO);
    }

    /**
     * 비율로부터 백분율 생성 (0.15 -> 15%)
     */
    public static Percentage fromRatio(BigDecimal ratio) {
        if (ratio == null) {
            throw new InvalidValueObjectException("비율 값은 필수입니다.");
        }
        return new Percentage(ratio.multiply(ONE_HUNDRED));
    }

    /**
     * 분자와 분모로부터 백분율 계산
     */
    public static Percentage calculate(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null) {
            throw new InvalidValueObjectException("분자와 분모는 필수입니다.");
        }

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return zero();
        }

        BigDecimal ratio = numerator.divide(denominator, 4, DEFAULT_ROUNDING);
        return fromRatio(ratio);
    }

    /**
     * 백분율을 비율로 변환 (15% -> 0.15)
     */
    public BigDecimal toRatio() {
        return value.divide(ONE_HUNDRED, 4, DEFAULT_ROUNDING);
    }

    /**
     * 양수인지 확인
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 음수인지 확인
     */
    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 0인지 확인
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 절댓값 반환
     */
    public Percentage abs() {
        return new Percentage(value.abs());
    }

    @Override
    public String toString() {
        return value.toPlainString() + "%";
    }
}