package com.export.dashboard.application.dto;

import com.export.dashboard.domain.model.ProductCategory;

/**
 * 상품 카테고리 정보 응답 DTO
 */
public record ProductCategoryResponse(
    Long id,
    String hsCode,
    Integer hsLevel,
    String nameKo,
    String nameEn,
    String parentHsCode,
    String description,
    Boolean active
) {
    public static ProductCategoryResponse from(ProductCategory productCategory) {
        return new ProductCategoryResponse(
            productCategory.getId(),
            productCategory.getHsCode().value(),
            productCategory.getHsCode().level(),
            productCategory.getNameKo(),
            productCategory.getNameEn(),
            productCategory.getParentHsCode(),
            productCategory.getDescription(),
            productCategory.isActive()
        );
    }
}