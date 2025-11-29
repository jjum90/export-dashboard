package com.export.dashboard.domain.event;

/**
 * 국가 관련 이벤트 데이터
 */
public record CountryData(
    String countryCode,
    String nameKo,
    String nameEn,
    String region,
    String continent,
    boolean active
) {

    public CountryData {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("국가 코드는 필수입니다");
        }
        if (nameKo == null || nameKo.trim().isEmpty()) {
            throw new IllegalArgumentException("한국어 국가명은 필수입니다");
        }
        if (nameEn == null || nameEn.trim().isEmpty()) {
            throw new IllegalArgumentException("영어 국가명은 필수입니다");
        }
    }
}