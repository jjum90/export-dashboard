package com.export.dashboard.domain.exception;

/**
 * Value Object 생성 시 유효성 검증 실패 예외
 */
public class InvalidValueObjectException extends DomainException {

    public InvalidValueObjectException(String message) {
        super(message);
    }

    public InvalidValueObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}