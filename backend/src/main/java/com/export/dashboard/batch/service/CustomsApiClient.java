package com.export.dashboard.batch.service;

import com.export.dashboard.batch.model.CustomsApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 관세청 API Feign Client
 * 관세청 공공 API를 통해 무역 통계 데이터 조회
 */
@FeignClient(
    name = "customs-api",
    url = "${customs.api.base-url}",
    configuration = com.export.dashboard.batch.config.FeignClientConfiguration.class
)
public interface CustomsApiClient {

    /**
     * 무역통계 조회 API
     *
     * @param serviceKey API 인증키
     * @param strtYymm   시작년월 (YYYYMM)
     * @param endYymm    종료년월 (YYYYMM)
     * @param hsSgn      품목코드 (선택, HS코드)
     * @return 무역 통계 응답
     */
    @GetMapping("/TexpimpMtYyQy")
    CustomsApiResponse getTradeStatistics(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("strtYymm") String strtYymm,
        @RequestParam("endYymm") String endYymm,
        @RequestParam(value = "hsSgn", required = false) String hsSgn
    );
}
