package com.export.dashboard.infrastructure.persistence;

import com.export.dashboard.domain.model.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Money Value Object를 위한 JPA AttributeConverter
 * 주의: Money는 amount와 currency 두 개의 필드를 가지므로 @Embeddable을 사용하는 것이 더 적절함
 * 이 컨버터는 amount만 저장하고 currency는 별도 컬럼에서 관리
 */
@Converter
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        if (money == null) {
            return null;
        }
        return money.amount();
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        // currency는 별도로 관리되므로 기본값 USD 사용
        return Money.usd(amount);
    }
}