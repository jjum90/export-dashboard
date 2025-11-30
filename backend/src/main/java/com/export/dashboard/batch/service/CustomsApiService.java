package com.export.dashboard.batch.service;

import com.export.dashboard.batch.config.CustomsApiProperties;
import com.export.dashboard.batch.exception.CustomsApiException;
import com.export.dashboard.batch.model.CustomsApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 관세청 API 호출 서비스
 * Circuit Breaker, Retry, Rate Limiter 패턴 적용
 */
@Service
public class CustomsApiService {

    private static final Logger logger = LoggerFactory.getLogger(CustomsApiService.class);
    private static final String CIRCUIT_BREAKER_NAME = "customsApi";

    private final CustomsApiClient customsApiClient;
    private final CustomsApiProperties customsApiProperties;

    public CustomsApiService(
        CustomsApiClient customsApiClient,
        CustomsApiProperties customsApiProperties
    ) {
        this.customsApiClient = customsApiClient;
        this.customsApiProperties = customsApiProperties;
    }

    /**
     * 무역 통계 데이터 조회
     *
     * @param startYearMonth 시작년월 (YYYYMM)
     * @param endYearMonth   종료년월 (YYYYMM)
     * @param hsCode         HS코드 (선택)
     * @return 무역 통계 응답
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getTradeStatisticsFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @RateLimiter(name = CIRCUIT_BREAKER_NAME)
    public CustomsApiResponse getTradeStatistics(
        String startYearMonth,
        String endYearMonth,
        String hsCode
    ) {
        logger.info("관세청 API 호출: startYymm={}, endYymm={}, hsCode={}",
            startYearMonth, endYearMonth, hsCode);

        try {
            String serviceKey = customsApiProperties.getServiceKey();

            if (serviceKey == null || serviceKey.isEmpty() || "YOUR_API_KEY".equals(serviceKey)) {
                throw new CustomsApiException("관세청 API 서비스 키가 설정되지 않았습니다.");
            }

            CustomsApiResponse response = customsApiClient.getTradeStatistics(
                serviceKey,
                startYearMonth,
                endYearMonth,
                hsCode
            );

            if (response == null || response.getTradeDataList() == null) {
                logger.warn("관세청 API 응답이 비어있습니다: hsCode={}", hsCode);
                return createEmptyResponse();
            }

            logger.info("관세청 API 응답 수신: {} 건", response.getTradeDataList().size());
            return response;

        } catch (Exception e) {
            logger.error("관세청 API 호출 실패: hsCode={}, error={}", hsCode, e.getMessage(), e);
            throw new CustomsApiException("관세청 API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Circuit Breaker Fallback 메서드
     * API 호출 실패 시 빈 응답 반환
     */
    @SuppressWarnings("unused")
    private CustomsApiResponse getTradeStatisticsFallback(
        String startYearMonth,
        String endYearMonth,
        String hsCode,
        Throwable t
    ) {
        logger.error("관세청 API Circuit Breaker Fallback 실행: hsCode={}, error={}",
            hsCode, t.getMessage());

        // 빈 응답 반환 (배치 작업 계속 진행)
        return createEmptyResponse();
    }

    /**
     * 빈 응답 객체 생성
     */
    private CustomsApiResponse createEmptyResponse() {
        CustomsApiResponse response = new CustomsApiResponse();
        response.setTradeDataList(java.util.Collections.emptyList());
        return response;
    }

    /**
     * API 호출 전 서비스 키 유효성 검증
     */
    public boolean isApiKeyConfigured() {
        String serviceKey = customsApiProperties.getServiceKey();
        return serviceKey != null
            && !serviceKey.isEmpty()
            && !"YOUR_API_KEY".equals(serviceKey);
    }
}
