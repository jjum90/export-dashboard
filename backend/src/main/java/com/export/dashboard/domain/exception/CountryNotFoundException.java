package com.export.dashboard.domain.exception;

/**
 * 국가 정보를 찾을 수 없을 때 발생하는 예외
 */
public class CountryNotFoundException extends DomainException {

    public CountryNotFoundException(String message) {
        super(message);
    }

    public static CountryNotFoundException withId(Long id) {
        return new CountryNotFoundException("국가를 찾을 수 없습니다. ID: " + id);
    }

    public static CountryNotFoundException withCode(String countryCode) {
        return new CountryNotFoundException("국가를 찾을 수 없습니다. 국가 코드: " + countryCode);
    }
}