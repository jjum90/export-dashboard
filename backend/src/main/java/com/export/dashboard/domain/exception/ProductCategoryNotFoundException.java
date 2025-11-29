package com.export.dashboard.domain.exception;

/**
 * 상품 카테고리를 찾을 수 없을 때 발생하는 예외
 */
public class ProductCategoryNotFoundException extends DomainException {

    public ProductCategoryNotFoundException(String message) {
        super(message);
    }

    public static ProductCategoryNotFoundException withId(Long id) {
        return new ProductCategoryNotFoundException("상품 카테고리를 찾을 수 없습니다. ID: " + id);
    }

    public static ProductCategoryNotFoundException withHsCode(String hsCode) {
        return new ProductCategoryNotFoundException("상품 카테고리를 찾을 수 없습니다. HS 코드: " + hsCode);
    }
}