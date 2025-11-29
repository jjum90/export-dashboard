package com.export.dashboard.domain.repository;

import com.export.dashboard.domain.model.HsCode;
import com.export.dashboard.domain.model.ProductCategory;

import java.util.List;
import java.util.Optional;

/**
 * ProductCategory Aggregate의 도메인 리포지토리 인터페이스
 */
public interface ProductCategoryRepository {

    /**
     * 상품 카테고리 저장
     */
    ProductCategory save(ProductCategory productCategory);

    /**
     * ID로 상품 카테고리 조회
     */
    Optional<ProductCategory> findById(Long id);

    /**
     * HS 코드로 상품 카테고리 조회
     */
    Optional<ProductCategory> findByHsCode(HsCode hsCode);

    /**
     * 활성화된 모든 상품 카테고리 조회
     */
    List<ProductCategory> findAllActive();

    /**
     * HS 코드 레벨별 상품 카테고리 조회
     */
    List<ProductCategory> findByLevel(Integer level);

    /**
     * 부모 HS 코드로 하위 카테고리 조회
     */
    List<ProductCategory> findByParentHsCode(String parentHsCode);

    /**
     * 챕터(2자리 HS 코드)로 시작하는 모든 카테고리 조회
     */
    List<ProductCategory> findByChapter(String chapterCode);

    /**
     * HS 코드 존재 여부 확인
     */
    boolean existsByHsCode(HsCode hsCode);

    /**
     * 상품 카테고리 삭제
     */
    void delete(ProductCategory productCategory);

    /**
     * 모든 상품 카테고리 조회
     */
    List<ProductCategory> findAll();
}