package com.export.dashboard.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 수출 통계 생성 요청 DTO
 */
public record CreateExportStatisticRequest(
    @NotNull(message = "국가 ID는 필수입니다.")
    Long countryId,

    @NotNull(message = "상품 카테고리 ID는 필수입니다.")
    Long productCategoryId,

    @NotNull(message = "년도는 필수입니다.")
    @Min(value = 1900, message = "년도는 1900년 이상이어야 합니다.")
    @Max(value = 2100, message = "년도는 2100년 이하여야 합니다.")
    Integer year,

    @NotNull(message = "월은 필수입니다.")
    @Min(value = 1, message = "월은 1 이상이어야 합니다.")
    @Max(value = 12, message = "월은 12 이하여야 합니다.")
    Integer month,

    @NotNull(message = "수출 금액은 필수입니다.")
    @DecimalMin(value = "0.0", message = "수출 금액은 0 이상이어야 합니다.")
    BigDecimal exportValueUsd,

    @DecimalMin(value = "0.0", message = "수출 중량은 0 이상이어야 합니다.")
    BigDecimal exportWeightKg,

    @DecimalMin(value = "0.0", message = "수출 수량은 0 이상이어야 합니다.")
    BigDecimal exportQuantity,

    @Size(max = 20, message = "수량 단위는 20자를 초과할 수 없습니다.")
    String quantityUnit
) {}