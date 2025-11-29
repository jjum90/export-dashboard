package com.export.dashboard.interfaces.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 오류 응답을 위한 Record 클래스
 * 일관된 오류 응답 형식을 제공
 */
public record ErrorResponse(
    String message,
    String errorCode,
    int status,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp,
    String path,
    List<FieldError> fieldErrors
) {

    public static ErrorResponse of(String message, String errorCode, int status, String path) {
        return new ErrorResponse(
            message,
            errorCode,
            status,
            LocalDateTime.now(),
            path,
            null
        );
    }

    public static ErrorResponse of(String message, String errorCode, int status, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(
            message,
            errorCode,
            status,
            LocalDateTime.now(),
            path,
            fieldErrors
        );
    }

    /**
     * 필드별 유효성 검증 오류 정보
     */
    public record FieldError(
        String field,
        Object rejectedValue,
        String message
    ) {
        public static FieldError of(String field, Object rejectedValue, String message) {
            return new FieldError(field, rejectedValue, message);
        }
    }
}