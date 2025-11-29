package com.export.dashboard.infrastructure.repository;

import com.export.dashboard.domain.model.Country;
import com.export.dashboard.domain.model.CountryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Country JPA Repository 인터페이스
 */
public interface JpaCountryRepository extends JpaRepository<Country, Long> {

    @Query("SELECT c FROM Country c WHERE c.countryCode.value = :countryCode")
    Optional<Country> findByCountryCode(@Param("countryCode") String countryCode);

    @Query("SELECT c FROM Country c WHERE c.active = true ORDER BY c.nameEn")
    List<Country> findAllActive();

    @Query("SELECT c FROM Country c WHERE c.region = :region AND c.active = true ORDER BY c.nameEn")
    List<Country> findByRegion(@Param("region") String region);

    @Query("SELECT c FROM Country c WHERE c.continent = :continent AND c.active = true ORDER BY c.nameEn")
    List<Country> findByContinent(@Param("continent") String continent);

    @Query("SELECT COUNT(c) > 0 FROM Country c WHERE c.countryCode.value = :countryCode")
    boolean existsByCountryCode(@Param("countryCode") String countryCode);
}