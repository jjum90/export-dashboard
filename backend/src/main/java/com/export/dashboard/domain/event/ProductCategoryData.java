package com.export.dashboard.domain.event;

/**
 * 상품 카테고리 관련 이벤트 데이터
 */
public record ProductCategoryData(
    String hsCode,
    int level,
    String nameKo,
    String nameEn,
    String parentHsCode,
    String description
) {

    public ProductCategoryData {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            throw new IllegalArgumentException("HS 코드는 필수입니다");
        }
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("HS 코드 레벨은 1-6 사이여야 합니다");
        }
        if (nameKo == null || nameKo.trim().isEmpty()) {
            throw new IllegalArgumentException("한국어 카테고리명은 필수입니다");
        }
        if (nameEn == null || nameEn.trim().isEmpty()) {
            throw new IllegalArgumentException("영어 카테고리명은 필수입니다");
        }
    }
}