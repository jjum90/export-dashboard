package com.export.dashboard.infrastructure.repository;

import com.export.dashboard.domain.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ProductCategory JPA Repository 인터페이스
 */
public interface JpaProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("SELECT p FROM ProductCategory p WHERE p.hsCode.value = :hsCode")
    Optional<ProductCategory> findByHsCode(@Param("hsCode") String hsCode);

    @Query("SELECT p FROM ProductCategory p WHERE p.active = true ORDER BY p.hsCode.value")
    List<ProductCategory> findAllActive();

    @Query("SELECT p FROM ProductCategory p WHERE p.hsCode.level = :level AND p.active = true ORDER BY p.hsCode.value")
    List<ProductCategory> findByLevel(@Param("level") Integer level);

    @Query("SELECT p FROM ProductCategory p WHERE p.parentHsCode = :parentHsCode AND p.active = true ORDER BY p.hsCode.value")
    List<ProductCategory> findByParentHsCode(@Param("parentHsCode") String parentHsCode);

    @Query("SELECT p FROM ProductCategory p WHERE p.hsCode.value LIKE :chapterCode% AND p.active = true ORDER BY p.hsCode.value")
    List<ProductCategory> findByChapter(@Param("chapterCode") String chapterCode);

    @Query("SELECT COUNT(p) > 0 FROM ProductCategory p WHERE p.hsCode.value = :hsCode")
    boolean existsByHsCode(@Param("hsCode") String hsCode);
}