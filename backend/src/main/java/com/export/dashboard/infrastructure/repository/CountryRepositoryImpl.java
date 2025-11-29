package com.export.dashboard.infrastructure.repository;

import com.export.dashboard.domain.model.Country;
import com.export.dashboard.domain.model.CountryCode;
import com.export.dashboard.domain.repository.CountryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Country Repository 구현체
 * 도메인 Repository 인터페이스를 JPA Repository로 구현
 */
@Repository
public class CountryRepositoryImpl implements CountryRepository {

    private final JpaCountryRepository jpaCountryRepository;

    public CountryRepositoryImpl(JpaCountryRepository jpaCountryRepository) {
        this.jpaCountryRepository = jpaCountryRepository;
    }

    @Override
    public Country save(Country country) {
        return jpaCountryRepository.save(country);
    }

    @Override
    public Optional<Country> findById(Long id) {
        return jpaCountryRepository.findById(id);
    }

    @Override
    public Optional<Country> findByCountryCode(CountryCode countryCode) {
        return jpaCountryRepository.findByCountryCode(countryCode.value());
    }

    @Override
    public List<Country> findAllActive() {
        return jpaCountryRepository.findAllActive();
    }

    @Override
    public List<Country> findByRegion(String region) {
        return jpaCountryRepository.findByRegion(region);
    }

    @Override
    public List<Country> findByContinent(String continent) {
        return jpaCountryRepository.findByContinent(continent);
    }

    @Override
    public boolean existsByCountryCode(CountryCode countryCode) {
        return jpaCountryRepository.existsByCountryCode(countryCode.value());
    }

    @Override
    public void delete(Country country) {
        jpaCountryRepository.delete(country);
    }

    @Override
    public List<Country> findAll() {
        return jpaCountryRepository.findAll();
    }
}