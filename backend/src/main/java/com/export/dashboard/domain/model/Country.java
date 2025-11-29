package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import com.export.dashboard.domain.event.CountryActivated;
import com.export.dashboard.domain.event.CountryDeactivated;
import com.export.dashboard.domain.event.CountryData;
import com.export.dashboard.domain.event.DomainEvent;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 국가 Aggregate Root
 * 국가 정보를 관리하고 도메인 로직을 캡슐화
 */
@Entity
@Table(name = "countries")
public class Country extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "country_code", unique = true, nullable = false, length = 3))
    private CountryCode countryCode;

    @Column(name = "country_name_ko", nullable = false, length = 100)
    private String nameKo;

    @Column(name = "country_name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "continent", length = 30)
    private String continent;

    @Column(name = "is_active")
    private Boolean active = true;

    @Version
    private Long version;

    protected Country() {
        // JPA 전용
    }

    public Country(CountryCode countryCode, String nameKo, String nameEn) {
        this.countryCode = validateCountryCode(countryCode);
        this.nameKo = validateName(nameKo, "한국어 국가명");
        this.nameEn = validateName(nameEn, "영어 국가명");
        this.active = true;
    }

    public Country(CountryCode countryCode, String nameKo, String nameEn, String region, String continent) {
        this(countryCode, nameKo, nameEn);
        this.region = region;
        this.continent = continent;
    }

    // Factory methods
    public static Country create(String countryCode, String nameKo, String nameEn) {
        return new Country(CountryCode.from(countryCode), nameKo, nameEn);
    }

    public static Country create(String countryCode, String nameKo, String nameEn, String region, String continent) {
        return new Country(CountryCode.from(countryCode), nameKo, nameEn, region, continent);
    }

    // Business methods
    public void updateNames(String nameKo, String nameEn) {
        this.nameKo = validateName(nameKo, "한국어 국가명");
        this.nameEn = validateName(nameEn, "영어 국가명");
    }

    public void updateGeographicInfo(String region, String continent) {
        this.region = region;
        this.continent = continent;
    }

    public void activate() {
        if (!Boolean.TRUE.equals(this.active)) {
            this.active = true;

            // 국가 활성화 도메인 이벤트 발행
            var eventData = new CountryData(
                this.countryCode.value(),
                this.nameKo,
                this.nameEn,
                this.region,
                this.continent,
                true
            );

            var event = new CountryActivated(
                UUID.randomUUID(),
                Instant.now(),
                getAggregateId(),
                eventData
            );

            registerEvent(event);
        }
    }

    public void deactivate() {
        if (Boolean.TRUE.equals(this.active)) {
            this.active = false;

            // 국가 비활성화 도메인 이벤트 발행
            var eventData = new CountryData(
                this.countryCode.value(),
                this.nameKo,
                this.nameEn,
                this.region,
                this.continent,
                false
            );

            var event = new CountryDeactivated(
                UUID.randomUUID(),
                Instant.now(),
                getAggregateId(),
                eventData
            );

            registerEvent(event);
        }
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean belongsToRegion(String targetRegion) {
        return region != null && region.equalsIgnoreCase(targetRegion);
    }

    public boolean belongsToContinent(String targetContinent) {
        return continent != null && continent.equalsIgnoreCase(targetContinent);
    }

    // Validation methods
    private CountryCode validateCountryCode(CountryCode countryCode) {
        if (countryCode == null) {
            throw new InvalidValueObjectException("국가 코드는 필수입니다.");
        }
        return countryCode;
    }

    private String validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidValueObjectException(fieldName + "은 필수입니다.");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 100) {
            throw new InvalidValueObjectException(fieldName + "은 100자를 초과할 수 없습니다.");
        }
        return trimmedName;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public String getNameKo() {
        return nameKo;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getRegion() {
        return region;
    }

    public String getContinent() {
        return continent;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public String getAggregateId() {
        return countryCode != null ? countryCode.value() : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Country country = (Country) obj;
        return Objects.equals(countryCode, country.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode);
    }

    @Override
    public String toString() {
        return String.format("Country[code=%s, nameKo=%s, nameEn=%s]",
            countryCode, nameKo, nameEn);
    }
}