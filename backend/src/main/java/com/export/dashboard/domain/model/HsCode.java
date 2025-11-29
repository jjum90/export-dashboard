package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;

/**
 * HS Code(Harmonized System Code)를 나타내는 Value Object
 * 2-10자리 숫자로 구성된 품목 분류 코드
 */
public record HsCode(String value, Integer level) {

    private static final String HS_CODE_PATTERN = "^\\d{2,10}$";

    public HsCode {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidValueObjectException("HS 코드는 필수입니다.");
        }

        String trimmedValue = value.trim();
        if (!trimmedValue.matches(HS_CODE_PATTERN)) {
            throw new InvalidValueObjectException(
                "HS 코드는 2-10자리 숫자여야 합니다. 입력값: " + value
            );
        }

        if (level == null || level < 1 || level > 6) {
            throw new InvalidValueObjectException(
                "HS 코드 레벨은 1-6 사이여야 합니다. 입력값: " + level
            );
        }

        // HS 코드 레벨과 자릿수 일치성 검증
        int expectedLength = level * 2;
        if (trimmedValue.length() != expectedLength) {
            throw new InvalidValueObjectException(
                String.format("HS 코드 레벨 %d는 %d자리여야 합니다. 입력값: %s (%d자리)",
                    level, expectedLength, value, trimmedValue.length())
            );
        }

        value = trimmedValue;
    }

    public static HsCode from(String code, Integer level) {
        return new HsCode(code, level);
    }

    public static HsCode of(String code, Integer level) {
        return new HsCode(code, level);
    }

    /**
     * 상위 레벨 HS 코드 반환
     */
    public HsCode getParent() {
        if (level <= 1) {
            throw new InvalidValueObjectException("레벨 1 HS 코드는 상위 코드가 없습니다.");
        }

        int parentLength = (level - 1) * 2;
        String parentCode = value.substring(0, parentLength);
        return new HsCode(parentCode, level - 1);
    }

    /**
     * 최상위 레벨(Chapter) HS 코드 반환
     */
    public HsCode getChapter() {
        if (level == 1) {
            return this;
        }
        return new HsCode(value.substring(0, 2), 1);
    }

    @Override
    public String toString() {
        return value;
    }
}