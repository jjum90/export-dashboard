package com.export.dashboard.batch.exception;

/**
 * Excel 파일 처리 중 발생하는 예외
 */
public class ExcelProcessingException extends RuntimeException {

    public ExcelProcessingException(String message) {
        super(message);
    }

    public ExcelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
