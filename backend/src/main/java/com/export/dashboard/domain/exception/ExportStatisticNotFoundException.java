package com.export.dashboard.domain.exception;

/**
 * 수출 통계 데이터를 찾을 수 없을 때 발생하는 예외
 */
public class ExportStatisticNotFoundException extends DomainException {

    public ExportStatisticNotFoundException(String message) {
        super(message);
    }

    public static ExportStatisticNotFoundException withId(Long id) {
        return new ExportStatisticNotFoundException("수출 통계를 찾을 수 없습니다. ID: " + id);
    }

    public static ExportStatisticNotFoundException withCriteria(String criteria) {
        return new ExportStatisticNotFoundException("수출 통계를 찾을 수 없습니다. 검색 조건: " + criteria);
    }
}