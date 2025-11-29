package com.export.dashboard.application.dto;

import com.export.dashboard.domain.model.Country;

/**
 * 국가 정보 응답 DTO
 */
public record CountryResponse(
    Long id,
    String countryCode,
    String nameKo,
    String nameEn,
    String region,
    String continent,
    Boolean active
) {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
            country.getId(),
            country.getCountryCode().value(),
            country.getNameKo(),
            country.getNameEn(),
            country.getRegion(),
            country.getContinent(),
            country.isActive()
        );
    }
}