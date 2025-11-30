package com.export.dashboard.batch.exception;

/**
 * 관세청 API 호출 중 발생하는 예외
 */
public class CustomsApiException extends RuntimeException {

    public CustomsApiException(String message) {
        super(message);
    }

    public CustomsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
