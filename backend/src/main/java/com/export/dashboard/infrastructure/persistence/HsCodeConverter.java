package com.export.dashboard.infrastructure.persistence;

import com.export.dashboard.domain.model.HsCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * HsCode Value Object를 위한 JPA AttributeConverter
 * 주의: HsCode는 value와 level 두 개의 필드를 가지므로 @Embeddable을 사용하는 것이 더 적절함
 * 이 컨버터는 value만 저장하고 level은 별도 컬럼에서 관리
 */
@Converter
public class HsCodeConverter implements AttributeConverter<HsCode, String> {

    @Override
    public String convertToDatabaseColumn(HsCode hsCode) {
        if (hsCode == null) {
            return null;
        }
        return hsCode.value();
    }

    @Override
    public HsCode convertToEntityAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        // level은 별도로 관리되므로 여기서는 추정
        int level = estimateLevel(value);
        return HsCode.from(value, level);
    }

    private int estimateLevel(String hsCode) {
        if (hsCode == null) return 1;
        int length = hsCode.length();
        return Math.min((length + 1) / 2, 6);
    }
}