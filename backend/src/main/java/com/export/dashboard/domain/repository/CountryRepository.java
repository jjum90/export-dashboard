package com.export.dashboard.domain.repository;

import com.export.dashboard.domain.model.Country;
import com.export.dashboard.domain.model.CountryCode;

import java.util.List;
import java.util.Optional;

/**
 * Country Aggregate의 도메인 리포지토리 인터페이스
 */
public interface CountryRepository {

    /**
     * 국가 저장
     */
    Country save(Country country);

    /**
     * ID로 국가 조회
     */
    Optional<Country> findById(Long id);

    /**
     * 국가 코드로 국가 조회
     */
    Optional<Country> findByCountryCode(CountryCode countryCode);

    /**
     * 활성화된 모든 국가 조회
     */
    List<Country> findAllActive();

    /**
     * 지역별 국가 조회
     */
    List<Country> findByRegion(String region);

    /**
     * 대륙별 국가 조회
     */
    List<Country> findByContinent(String continent);

    /**
     * 국가 코드 존재 여부 확인
     */
    boolean existsByCountryCode(CountryCode countryCode);

    /**
     * 국가 삭제
     */
    void delete(Country country);

    /**
     * 모든 국가 조회
     */
    List<Country> findAll();
}