package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * 금액을 나타내는 Value Object
 * 통화와 금액을 함께 관리하여 통화 혼용 오류를 방지
 */
public record Money(BigDecimal amount, Currency currency) {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    public Money {
        if (amount == null) {
            throw new InvalidValueObjectException("금액은 필수입니다.");
        }

        if (currency == null) {
            throw new InvalidValueObjectException("통화는 필수입니다.");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidValueObjectException("금액은 0 이상이어야 합니다. 입력값: " + amount);
        }

        // 스케일 정규화
        amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money usd(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }

    public static Money krw(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("KRW"));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * 같은 통화끼리 더하기
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * 같은 통화끼리 빼기
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidValueObjectException("결과가 음수가 될 수 없습니다.");
        }
        return new Money(result, this.currency);
    }

    /**
     * 곱하기
     */
    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new InvalidValueObjectException("승수는 필수입니다.");
        }
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidValueObjectException("승수는 0 이상이어야 합니다.");
        }
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    /**
     * 나누기
     */
    public Money divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new InvalidValueObjectException("제수는 필수입니다.");
        }
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidValueObjectException("제수는 0보다 커야 합니다.");
        }
        return new Money(this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING), this.currency);
    }

    /**
     * 백분율 계산 (이 금액이 전체에서 차지하는 비율)
     */
    public BigDecimal percentageOf(Money total) {
        validateSameCurrency(total);
        if (total.amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.amount.divide(total.amount, 4, DEFAULT_ROUNDING)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 0인지 확인
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 양수인지 확인
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvalidValueObjectException(
                String.format("통화가 일치하지 않습니다. %s != %s",
                    this.currency.getCurrencyCode(), other.currency.getCurrencyCode())
            );
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount.toPlainString(), currency.getCurrencyCode());
    }
}