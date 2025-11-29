package com.export.dashboard.interfaces.web;

import com.export.dashboard.application.dto.*;
import com.export.dashboard.application.service.ExportStatisticApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 수출 통계 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/export-statistics")
@CrossOrigin(origins = "*")
public class ExportStatisticController {

    private final ExportStatisticApplicationService exportStatisticApplicationService;

    public ExportStatisticController(ExportStatisticApplicationService exportStatisticApplicationService) {
        this.exportStatisticApplicationService = exportStatisticApplicationService;
    }

    /**
     * 새로운 수출 통계 생성
     */
    @PostMapping
    public ResponseEntity<ExportStatisticResponse> createExportStatistic(
            @Valid @RequestBody CreateExportStatisticRequest request) {
        ExportStatisticResponse response = exportStatisticApplicationService.createExportStatistic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 수출 통계 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExportStatisticResponse> getExportStatisticById(@PathVariable Long id) {
        ExportStatisticResponse response = exportStatisticApplicationService.getExportStatisticById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 년도별 수출 통계 조회
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<List<ExportStatisticResponse>> getExportStatisticsByYear(@PathVariable Integer year) {
        List<ExportStatisticResponse> responses = exportStatisticApplicationService.getExportStatisticsByYear(year);
        return ResponseEntity.ok(responses);
    }

    /**
     * 국가와 년도별 수출 통계 조회
     */
    @GetMapping("/country/{countryId}/year/{year}")
    public ResponseEntity<List<ExportStatisticResponse>> getExportStatisticsByCountryAndYear(
            @PathVariable Long countryId, @PathVariable Integer year) {
        List<ExportStatisticResponse> responses =
            exportStatisticApplicationService.getExportStatisticsByCountryAndYear(countryId, year);
        return ResponseEntity.ok(responses);
    }

    /**
     * 페이징된 수출 통계 조회
     */
    @GetMapping
    public ResponseEntity<Page<ExportStatisticResponse>> getExportStatistics(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ExportStatisticResponse> responses = exportStatisticApplicationService.getExportStatistics(pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * 대시보드 요약 정보 조회
     */
    @GetMapping("/dashboard/{year}")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@PathVariable Integer year) {
        DashboardSummaryResponse response = exportStatisticApplicationService.getDashboardSummary(year);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용 가능한 년도 목록 조회
     */
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAvailableYears() {
        List<Integer> years = exportStatisticApplicationService.getAvailableYears();
        return ResponseEntity.ok(years);
    }
}