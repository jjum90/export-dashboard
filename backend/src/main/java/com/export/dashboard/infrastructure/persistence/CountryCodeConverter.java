package com.export.dashboard.infrastructure.persistence;

import com.export.dashboard.domain.model.CountryCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CountryCode Value Object를 위한 JPA AttributeConverter
 */
@Converter(autoApply = true)
public class CountryCodeConverter implements AttributeConverter<CountryCode, String> {

    @Override
    public String convertToDatabaseColumn(CountryCode countryCode) {
        if (countryCode == null) {
            return null;
        }
        return countryCode.value();
    }

    @Override
    public CountryCode convertToEntityAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return CountryCode.from(value);
    }
}