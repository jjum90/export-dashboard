package com.export.dashboard.interfaces.exception;

import com.export.dashboard.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 모든 예외를 적절한 HTTP 응답으로 변환하여 일관된 오류 응답을 제공
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 도메인 Value Object 유효성 검증 실패
     */
    @ExceptionHandler(InvalidValueObjectException.class)
    public ResponseEntity<ErrorResponse> handleInvalidValueObjectException(
            InvalidValueObjectException ex, HttpServletRequest request) {
        logger.warn("Invalid value object: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "INVALID_VALUE_OBJECT",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 수출 통계 데이터를 찾을 수 없음
     */
    @ExceptionHandler(ExportStatisticNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExportStatisticNotFoundException(
            ExportStatisticNotFoundException ex, HttpServletRequest request) {
        logger.warn("Export statistic not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "EXPORT_STATISTIC_NOT_FOUND",
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI()
        );

        return ResponseEntity.notFound().build();
    }

    /**
     * 국가 정보를 찾을 수 없음
     */
    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCountryNotFoundException(
            CountryNotFoundException ex, HttpServletRequest request) {
        logger.warn("Country not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "COUNTRY_NOT_FOUND",
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 상품 카테고리를 찾을 수 없음
     */
    @ExceptionHandler(ProductCategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductCategoryNotFoundException(
            ProductCategoryNotFoundException ex, HttpServletRequest request) {
        logger.warn("Product category not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "PRODUCT_CATEGORY_NOT_FOUND",
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 기타 도메인 예외
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        logger.warn("Domain exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "DOMAIN_ERROR",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Bean Validation 유효성 검증 실패 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("Validation failed: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.of(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            "유효성 검증에 실패했습니다.",
            "VALIDATION_FAILED",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Bean Validation 유효성 검증 실패 (BindException)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {
        logger.warn("Binding validation failed: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.of(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            "데이터 바인딩에 실패했습니다.",
            "BINDING_FAILED",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Constraint Validation 유효성 검증 실패
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        logger.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
            .stream()
            .map(violation -> ErrorResponse.FieldError.of(
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                violation.getMessage()
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            "제약 조건 위반입니다.",
            "CONSTRAINT_VIOLATION",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 잘못된 파라미터 타입
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.warn("Method argument type mismatch: {}", ex.getMessage());

        String message = String.format("파라미터 '%s'의 값 '%s'이 올바르지 않습니다. %s 타입이어야 합니다.",
            ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "INVALID_PARAMETER_TYPE",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 데이터 무결성 위반 (중복 키, 외래키 제약 등)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.warn("Data integrity violation: {}", ex.getMessage());

        String message = "데이터 무결성 제약 조건에 위반됩니다.";

        // 구체적인 오류 메시지 추출
        if (ex.getMessage().contains("duplicate key")) {
            message = "중복된 데이터입니다.";
        } else if (ex.getMessage().contains("foreign key")) {
            message = "참조 무결성 제약 조건에 위반됩니다.";
        } else if (ex.getMessage().contains("not-null")) {
            message = "필수 값이 누락되었습니다.";
        }

        ErrorResponse errorResponse = ErrorResponse.of(
            message,
            "DATA_INTEGRITY_VIOLATION",
            HttpStatus.CONFLICT.value(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 일반적인 IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "ILLEGAL_ARGUMENT",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 일반적인 IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        logger.warn("Illegal state: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getMessage(),
            "ILLEGAL_STATE",
            HttpStatus.CONFLICT.value(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 예상하지 못한 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            "내부 서버 오류가 발생했습니다.",
            "INTERNAL_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );

        return ResponseEntity.internalServerError().body(errorResponse);
    }
}