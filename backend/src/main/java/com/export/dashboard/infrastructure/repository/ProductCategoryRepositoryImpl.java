package com.export.dashboard.infrastructure.repository;

import com.export.dashboard.domain.model.HsCode;
import com.export.dashboard.domain.model.ProductCategory;
import com.export.dashboard.domain.repository.ProductCategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductCategory Repository 구현체
 * 도메인 Repository 인터페이스를 JPA Repository로 구현
 */
@Repository
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final JpaProductCategoryRepository jpaProductCategoryRepository;

    public ProductCategoryRepositoryImpl(JpaProductCategoryRepository jpaProductCategoryRepository) {
        this.jpaProductCategoryRepository = jpaProductCategoryRepository;
    }

    @Override
    public ProductCategory save(ProductCategory productCategory) {
        return jpaProductCategoryRepository.save(productCategory);
    }

    @Override
    public Optional<ProductCategory> findById(Long id) {
        return jpaProductCategoryRepository.findById(id);
    }

    @Override
    public Optional<ProductCategory> findByHsCode(HsCode hsCode) {
        return jpaProductCategoryRepository.findByHsCode(hsCode.value());
    }

    @Override
    public List<ProductCategory> findAllActive() {
        return jpaProductCategoryRepository.findAllActive();
    }

    @Override
    public List<ProductCategory> findByLevel(Integer level) {
        return jpaProductCategoryRepository.findByLevel(level);
    }

    @Override
    public List<ProductCategory> findByParentHsCode(String parentHsCode) {
        return jpaProductCategoryRepository.findByParentHsCode(parentHsCode);
    }

    @Override
    public List<ProductCategory> findByChapter(String chapterCode) {
        return jpaProductCategoryRepository.findByChapter(chapterCode);
    }

    @Override
    public boolean existsByHsCode(HsCode hsCode) {
        return jpaProductCategoryRepository.existsByHsCode(hsCode.value());
    }

    @Override
    public void delete(ProductCategory productCategory) {
        jpaProductCategoryRepository.delete(productCategory);
    }

    @Override
    public List<ProductCategory> findAll() {
        return jpaProductCategoryRepository.findAll();
    }
}