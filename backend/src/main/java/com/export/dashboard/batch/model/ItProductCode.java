package com.export.dashboard.batch.model;

import java.util.Objects;

/**
 * IT 부품 HS코드 정보
 * Excel 파일에서 로드한 IT 제품 코드 정보를 담는 모델
 */
public class ItProductCode {

    private final String hsCode;
    private final String productName;
    private final String description;

    public ItProductCode(String hsCode, String productName, String description) {
        this.hsCode = validateHsCode(hsCode);
        this.productName = validateProductName(productName);
        this.description = description;
    }

    private String validateHsCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            throw new IllegalArgumentException("HS 코드는 필수입니다.");
        }
        String trimmed = hsCode.trim();
        if (trimmed.length() < 2 || trimmed.length() > 10) {
            throw new IllegalArgumentException("HS 코드는 2~10자리여야 합니다: " + trimmed);
        }
        return trimmed;
    }

    private String validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("제품명은 필수입니다.");
        }
        return productName.trim();
    }

    public String getHsCode() {
        return hsCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItProductCode that = (ItProductCode) o;
        return Objects.equals(hsCode, that.hsCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hsCode);
    }

    @Override
    public String toString() {
        return String.format("ItProductCode[hsCode=%s, productName=%s]", hsCode, productName);
    }
}
