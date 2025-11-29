package com.export.dashboard.interfaces.web;

import com.export.dashboard.application.dto.CountryResponse;
import com.export.dashboard.application.dto.CreateCountryRequest;
import com.export.dashboard.application.service.CountryApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 국가 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/countries")
@CrossOrigin(origins = "*")
public class CountryController {

    private final CountryApplicationService countryApplicationService;

    public CountryController(CountryApplicationService countryApplicationService) {
        this.countryApplicationService = countryApplicationService;
    }

    /**
     * 새로운 국가 생성
     */
    @PostMapping
    public ResponseEntity<CountryResponse> createCountry(@Valid @RequestBody CreateCountryRequest request) {
        CountryResponse response = countryApplicationService.createCountry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 국가 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CountryResponse> getCountryById(@PathVariable Long id) {
        CountryResponse response = countryApplicationService.getCountryById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 국가 코드로 조회
     */
    @GetMapping("/code/{countryCode}")
    public ResponseEntity<CountryResponse> getCountryByCode(@PathVariable String countryCode) {
        CountryResponse response = countryApplicationService.getCountryByCode(countryCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 활성화된 모든 국가 조회
     */
    @GetMapping
    public ResponseEntity<List<CountryResponse>> getAllActiveCountries() {
        List<CountryResponse> responses = countryApplicationService.getAllActiveCountries();
        return ResponseEntity.ok(responses);
    }

    /**
     * 지역별 국가 조회
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<List<CountryResponse>> getCountriesByRegion(@PathVariable String region) {
        List<CountryResponse> responses = countryApplicationService.getCountriesByRegion(region);
        return ResponseEntity.ok(responses);
    }

    /**
     * 대륙별 국가 조회
     */
    @GetMapping("/continent/{continent}")
    public ResponseEntity<List<CountryResponse>> getCountriesByContinent(@PathVariable String continent) {
        List<CountryResponse> responses = countryApplicationService.getCountriesByContinent(continent);
        return ResponseEntity.ok(responses);
    }

    /**
     * 국가 활성화
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCountry(@PathVariable Long id) {
        countryApplicationService.activateCountry(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 국가 비활성화
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCountry(@PathVariable Long id) {
        countryApplicationService.deactivateCountry(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 국가 조회 (관리자용)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<CountryResponse>> getAllCountries() {
        List<CountryResponse> responses = countryApplicationService.getAllCountries();
        return ResponseEntity.ok(responses);
    }
}