package com.export.dashboard.infrastructure.persistence;

import com.export.dashboard.domain.model.Percentage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/**
 * Percentage Value Object를 위한 JPA AttributeConverter
 */
@Converter(autoApply = true)
public class PercentageConverter implements AttributeConverter<Percentage, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Percentage percentage) {
        if (percentage == null) {
            return null;
        }
        return percentage.value();
    }

    @Override
    public Percentage convertToEntityAttribute(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return Percentage.of(value);
    }
}