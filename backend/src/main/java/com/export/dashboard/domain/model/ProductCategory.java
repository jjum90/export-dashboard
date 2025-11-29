package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * 상품 카테고리 Aggregate Root
 * HS 코드 기반의 상품 분류를 관리하고 도메인 로직을 캡슐화
 */
@Entity
@Table(name = "product_categories")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "hs_code", unique = true, nullable = false, length = 10)),
        @AttributeOverride(name = "level", column = @Column(name = "hs_level", nullable = false))
    })
    private HsCode hsCode;

    @Column(name = "category_name_ko", nullable = false, length = 200)
    private String nameKo;

    @Column(name = "category_name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "parent_hs_code", length = 10)
    private String parentHsCode;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean active = true;

    @Version
    private Long version;

    protected ProductCategory() {
        // JPA 전용
    }

    public ProductCategory(HsCode hsCode, String nameKo, String nameEn) {
        this.hsCode = validateHsCode(hsCode);
        this.nameKo = validateName(nameKo, "한국어 카테고리명");
        this.nameEn = validateName(nameEn, "영어 카테고리명");
        this.active = true;

        // 부모 HS 코드 자동 설정
        if (hsCode.level() > 1) {
            this.parentHsCode = hsCode.getParent().value();
        }
    }

    public ProductCategory(HsCode hsCode, String nameKo, String nameEn, String description) {
        this(hsCode, nameKo, nameEn);
        this.description = description;
    }

    // Factory methods
    public static ProductCategory create(String hsCode, Integer level, String nameKo, String nameEn) {
        return new ProductCategory(HsCode.from(hsCode, level), nameKo, nameEn);
    }

    public static ProductCategory create(String hsCode, Integer level, String nameKo, String nameEn, String description) {
        return new ProductCategory(HsCode.from(hsCode, level), nameKo, nameEn, description);
    }

    // Business methods
    public void updateNames(String nameKo, String nameEn) {
        this.nameKo = validateName(nameKo, "한국어 카테고리명");
        this.nameEn = validateName(nameEn, "영어 카테고리명");
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean hasParent() {
        return parentHsCode != null && !parentHsCode.trim().isEmpty();
    }

    public boolean isChapter() {
        return hsCode.level() == 1;
    }

    public boolean isHeading() {
        return hsCode.level() == 2;
    }

    public boolean isSubheading() {
        return hsCode.level() >= 3;
    }

    public boolean isChildOf(ProductCategory parent) {
        if (parent == null || !hasParent()) {
            return false;
        }
        return parentHsCode.equals(parent.hsCode.value());
    }

    public HsCode getChapterCode() {
        return hsCode.getChapter();
    }

    public HsCode getParentCode() {
        if (!hasParent()) {
            throw new InvalidValueObjectException("최상위 카테고리는 부모가 없습니다.");
        }
        return hsCode.getParent();
    }

    // Validation methods
    private HsCode validateHsCode(HsCode hsCode) {
        if (hsCode == null) {
            throw new InvalidValueObjectException("HS 코드는 필수입니다.");
        }
        return hsCode;
    }

    private String validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidValueObjectException(fieldName + "은 필수입니다.");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 200) {
            throw new InvalidValueObjectException(fieldName + "은 200자를 초과할 수 없습니다.");
        }
        return trimmedName;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public HsCode getHsCode() {
        return hsCode;
    }

    public String getNameKo() {
        return nameKo;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getParentHsCode() {
        return parentHsCode;
    }

    public String getDescription() {
        return description;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductCategory that = (ProductCategory) obj;
        return Objects.equals(hsCode, that.hsCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hsCode);
    }

    @Override
    public String toString() {
        return String.format("ProductCategory[hsCode=%s, nameKo=%s, nameEn=%s]",
            hsCode, nameKo, nameEn);
    }
}