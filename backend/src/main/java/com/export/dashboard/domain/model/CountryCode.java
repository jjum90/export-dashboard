package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;

/**
 * ISO 3166-1 alpha-3 국가 코드를 나타내는 Value Object
 * 3자리 대문자 알파벳 형태의 국가 코드를 보장
 */
public record CountryCode(String value) {

    private static final String COUNTRY_CODE_PATTERN = "^[A-Z]{3}$";

    public CountryCode {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidValueObjectException("국가 코드는 필수입니다.");
        }

        String trimmedValue = value.trim().toUpperCase();
        if (!trimmedValue.matches(COUNTRY_CODE_PATTERN)) {
            throw new InvalidValueObjectException(
                "국가 코드는 3자리 대문자 알파벳이어야 합니다. 입력값: " + value
            );
        }

        // canonical form으로 저장
        value = trimmedValue;
    }

    public static CountryCode from(String code) {
        return new CountryCode(code);
    }

    public static CountryCode of(String code) {
        return new CountryCode(code);
    }

    @Override
    public String toString() {
        return value;
    }
}