package com.export.dashboard.application.service;

import com.export.dashboard.application.dto.CountryResponse;
import com.export.dashboard.application.dto.CreateCountryRequest;
import com.export.dashboard.domain.exception.CountryNotFoundException;
import com.export.dashboard.domain.model.Country;
import com.export.dashboard.domain.model.CountryCode;
import com.export.dashboard.domain.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 국가 관리 애플리케이션 서비스
 * 국가 관련 유스케이스를 조율하고 트랜잭션 경계를 관리
 */
@Service
@Transactional(readOnly = true)
public class CountryApplicationService {

    private final CountryRepository countryRepository;

    public CountryApplicationService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    /**
     * 새로운 국가 생성
     */
    @Transactional
    public CountryResponse createCountry(CreateCountryRequest request) {
        // 중복 검증
        CountryCode countryCode = CountryCode.from(request.countryCode());
        if (countryRepository.existsByCountryCode(countryCode)) {
            throw new IllegalArgumentException("이미 존재하는 국가 코드입니다: " + request.countryCode());
        }

        // 도메인 객체 생성
        Country country = Country.create(
            request.countryCode(),
            request.nameKo(),
            request.nameEn(),
            request.region(),
            request.continent()
        );

        // 저장
        Country savedCountry = countryRepository.save(country);

        return CountryResponse.from(savedCountry);
    }

    /**
     * 국가 ID로 조회
     */
    public CountryResponse getCountryById(Long id) {
        Country country = countryRepository.findById(id)
            .orElseThrow(() -> CountryNotFoundException.withId(id));

        return CountryResponse.from(country);
    }

    /**
     * 국가 코드로 조회
     */
    public CountryResponse getCountryByCode(String countryCode) {
        CountryCode code = CountryCode.from(countryCode);
        Country country = countryRepository.findByCountryCode(code)
            .orElseThrow(() -> CountryNotFoundException.withCode(countryCode));

        return CountryResponse.from(country);
    }

    /**
     * 활성화된 모든 국가 조회
     */
    public List<CountryResponse> getAllActiveCountries() {
        return countryRepository.findAllActive()
            .stream()
            .map(CountryResponse::from)
            .toList();
    }

    /**
     * 지역별 국가 조회
     */
    public List<CountryResponse> getCountriesByRegion(String region) {
        return countryRepository.findByRegion(region)
            .stream()
            .map(CountryResponse::from)
            .toList();
    }

    /**
     * 대륙별 국가 조회
     */
    public List<CountryResponse> getCountriesByContinent(String continent) {
        return countryRepository.findByContinent(continent)
            .stream()
            .map(CountryResponse::from)
            .toList();
    }

    /**
     * 국가 정보 업데이트
     */
    @Transactional
    public CountryResponse updateCountry(Long id, CreateCountryRequest request) {
        Country country = countryRepository.findById(id)
            .orElseThrow(() -> CountryNotFoundException.withId(id));

        // 국가 코드가 변경되는 경우 중복 검증
        CountryCode newCountryCode = CountryCode.from(request.countryCode());
        if (!country.getCountryCode().equals(newCountryCode)) {
            if (countryRepository.existsByCountryCode(newCountryCode)) {
                throw new IllegalArgumentException("이미 존재하는 국가 코드입니다: " + request.countryCode());
            }
        }

        // 도메인 로직을 통한 업데이트
        country.updateNames(request.nameKo(), request.nameEn());
        country.updateGeographicInfo(request.region(), request.continent());

        Country savedCountry = countryRepository.save(country);

        return CountryResponse.from(savedCountry);
    }

    /**
     * 국가 활성화
     */
    @Transactional
    public CountryResponse activateCountry(Long id) {
        Country country = countryRepository.findById(id)
            .orElseThrow(() -> CountryNotFoundException.withId(id));

        country.activate();
        Country savedCountry = countryRepository.save(country);

        return CountryResponse.from(savedCountry);
    }

    /**
     * 국가 비활성화
     */
    @Transactional
    public CountryResponse deactivateCountry(Long id) {
        Country country = countryRepository.findById(id)
            .orElseThrow(() -> CountryNotFoundException.withId(id));

        country.deactivate();
        Country savedCountry = countryRepository.save(country);

        return CountryResponse.from(savedCountry);
    }

    /**
     * 국가 삭제 (물리적 삭제)
     */
    @Transactional
    public void deleteCountry(Long id) {
        Country country = countryRepository.findById(id)
            .orElseThrow(() -> CountryNotFoundException.withId(id));

        countryRepository.delete(country);
    }

    /**
     * 모든 국가 조회 (관리자용)
     */
    public List<CountryResponse> getAllCountries() {
        return countryRepository.findAll()
            .stream()
            .map(CountryResponse::from)
            .toList();
    }
}