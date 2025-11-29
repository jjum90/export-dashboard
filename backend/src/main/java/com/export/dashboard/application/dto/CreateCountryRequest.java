package com.export.dashboard.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 국가 생성 요청 DTO
 */
public record CreateCountryRequest(
    @NotBlank(message = "국가 코드는 필수입니다.")
    @Size(max = 3, message = "국가 코드는 3자리여야 합니다.")
    String countryCode,

    @NotBlank(message = "한국어 국가명은 필수입니다.")
    @Size(max = 100, message = "한국어 국가명은 100자를 초과할 수 없습니다.")
    String nameKo,

    @NotBlank(message = "영어 국가명은 필수입니다.")
    @Size(max = 100, message = "영어 국가명은 100자를 초과할 수 없습니다.")
    String nameEn,

    @Size(max = 50, message = "지역명은 50자를 초과할 수 없습니다.")
    String region,

    @Size(max = 30, message = "대륙명은 30자를 초과할 수 없습니다.")
    String continent
) {}